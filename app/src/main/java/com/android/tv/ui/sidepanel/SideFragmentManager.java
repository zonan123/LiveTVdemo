/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.ui.sidepanel;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;

import com.mediatek.wwtv.tvcenter.R;

/** Manages {@link SideFragment}s. */
public class SideFragmentManager implements AccessibilityStateChangeListener {
    private static final String FIRST_BACKSTACK_RECORD_NAME = "0";

    private final Activity mActivity;
    private final FragmentManager mFragmentManager;
    private final Runnable mPreShowRunnable;
    private final Runnable mPostHideRunnable;
    private ViewTreeObserver.OnGlobalLayoutListener mShowOnGlobalLayoutListener;

    // To get the count reliably while using popBackStack(),
    // instead of using getBackStackEntryCount() with popBackStackImmediate().
    private int mFragmentCount;
    //call show fragment by yourself
    private final View mPanel;
    private final Animator mShowAnimator;
    private final Animator mHideAnimator;

    //private final AutoHideScheduler mAutoHideScheduler;
    //private final long mShowDurationMillis;

    public SideFragmentManager(
            Activity activity, Runnable preShowRunnable, Runnable postHideRunnable) {
        mActivity = activity;
        mFragmentManager = mActivity.getFragmentManager();
        mPreShowRunnable = preShowRunnable;
        mPostHideRunnable = postHideRunnable;

        mPanel = mActivity.findViewById(R.id.side_panel);
        mShowAnimator = AnimatorInflater.loadAnimator(mActivity, R.animator.side_panel_enter);
        mShowAnimator.setTarget(mPanel);
        mHideAnimator = AnimatorInflater.loadAnimator(mActivity, R.animator.side_panel_exit);
        mHideAnimator.setTarget(mPanel);
        mHideAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Animation is still in running state at this point.
                        hideAllInternal();
                    }
                });

//        mShowDurationMillis =
//                mActivity.getResources().getInteger(R.integer.side_panel_show_duration);
        //mAutoHideScheduler = new AutoHideScheduler(activity, () -> hideAll(true));
    }

    public int getCount() {
        return mFragmentCount;
    }
    
    public void setCount(int count) {
    	mFragmentCount=count;
    }

    public boolean isActive() {
        return mFragmentCount != 0 && !isHiding();
    }

    public boolean isHiding() {
        return mHideAnimator.isStarted();
    }

    public void setVisibility(int visibility) {
        mPanel.setVisibility(visibility);
    }

    /** Shows the given {@link SideFragment}. */
    public void show(SideFragment sideFragment) {
        show(sideFragment, true);
    }

    /** Shows the given {@link SideFragment}. */
    public void show(SideFragment sideFragment, boolean showEnterAnimation) {
        if (isHiding()) {
            mHideAnimator.end();
        }
        boolean isFirst = (mFragmentCount == 0);
        Log.d("SideFragmentManager", "show() isFirst :"+isFirst);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (!isFirst) {
            ft.setCustomAnimations(
                    showEnterAnimation ? R.animator.side_panel_fragment_enter : 0,
                    R.animator.side_panel_fragment_exit,
                    R.animator.side_panel_fragment_pop_enter,
                    R.animator.side_panel_fragment_pop_exit);
        }
        Log.d("SideFragmentManager", "show() active"+mActivity);
        ft.replace(R.id.side_fragment_container, sideFragment);
        ft.addToBackStack(Integer.toString(mFragmentCount));
        Log.d("SideFragmentManager", "show() active"+mActivity);
        ft.commit();
        mFragmentCount++;

        if (isFirst) {
            // We should wait for fragment transition and intital layouting finished to start the
            // slide-in animation to prevent jankiness resulted by performing transition and
            // layouting at the same time with animation.
            mPanel.setVisibility(View.VISIBLE);
            mShowOnGlobalLayoutListener =
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mShowOnGlobalLayoutListener = null;
                            if (mPreShowRunnable != null) {
                                mPreShowRunnable.run();
                            }
                            mShowAnimator.start();
                        }
                    };
            mPanel.getViewTreeObserver().addOnGlobalLayoutListener(mShowOnGlobalLayoutListener);
        }
        scheduleHideAll();
        Log.d("SideFragmentManager", "show() mFragmentCount :"+mFragmentCount);
    }

    public void popSideFragment() {
        mFragmentCount--;
        Log.d("SideFragmentManager", "popSideFragment() mFragmentCount :"+mFragmentCount);
        if (!isActive()) {
            return;
        } else if (mFragmentCount == 0) {
            // Show closing animation with the last fragment.
            hideAll(true);
            return;
        }
        mFragmentManager.popBackStack();
    }

    public void popStackNumber() {
        mFragmentCount--;
        Log.d("SideFragmentManager", "popStackNumber() mFragmentCount :"+mFragmentCount);
        if (mFragmentCount == 0) {
            //mPanel.setVisibility(View.GONE);
            Log.d("SideFragmentManager", "popStackNumber() true ");
            hideAll(true);
            ((com.mediatek.wwtv.setting.LiveTvSetting)mActivity).show();
        }
    }

    public void hideAll(boolean withAnimation) {
        if (mShowAnimator.isStarted()) {
            mShowAnimator.end();
        }
        if (mShowOnGlobalLayoutListener != null) {
            // The show operation maybe requested but the show animator is not started yet, in this
            // case, we show still run mPreShowRunnable.
            mPanel.getViewTreeObserver().removeOnGlobalLayoutListener(mShowOnGlobalLayoutListener);
            mShowOnGlobalLayoutListener = null;
            if (mPreShowRunnable != null) {
                mPreShowRunnable.run();
            }
        }
        if (withAnimation) {
            if (!isHiding()) {
                mHideAnimator.start();
            }
            return;
        }
        if (isHiding()) {
            mHideAnimator.end();
            return;
        }
        hideAllInternal();
    }

    private void hideAllInternal() {
        //mAutoHideScheduler.cancel();
        Log.d("SideFragmentManager", "hideAllInternal() mFragmentCount :"+mFragmentCount);
        if (mFragmentCount == 0) {
            return;
        }

        mPanel.setVisibility(View.GONE);
        mFragmentManager.popBackStack(
                FIRST_BACKSTACK_RECORD_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mFragmentCount = 0;

        if (mPostHideRunnable != null) {
            mPostHideRunnable.run();
        }
        Log.d("SideFragmentManager", "hideAllInternal() mFragmentCount :"+mFragmentCount);
    }

    /**
     * Show the side panel with animation. If there are many entries in the fragment stack, the
     * animation look like that there's only one fragment.
     *
     * @param withAnimation specifies if animation should be shown.
     */
    public void showSidePanel(boolean withAnimation) {
        if (mFragmentCount == 0) {
            return;
        }

        mPanel.setVisibility(View.VISIBLE);
        if (withAnimation) {
            mShowAnimator.start();
        }
        scheduleHideAll();
    }

    /**
     * Hide the side panel. This method just hide the panel and preserves the back stack. If you
     * want to empty the back stack, call {@link #hideAll}.
     */
    public void hideSidePanel(boolean withAnimation) {
        //mAutoHideScheduler.cancel();
        if (withAnimation) {
            Animator hideAnimator =
                    AnimatorInflater.loadAnimator(mActivity, R.animator.side_panel_exit);
            hideAnimator.setTarget(mPanel);
            hideAnimator.start();
            hideAnimator.addListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mPanel.setVisibility(View.GONE);
                        }
                    });
        } else {
            mPanel.setVisibility(View.GONE);
        }
    }

    public boolean isSidePanelVisible() {
        return mPanel.getVisibility() == View.VISIBLE;
    }

    /** Resets the timer for hiding side fragment. */
    public void scheduleHideAll() {
        //mAutoHideScheduler.schedule(mShowDurationMillis);
    }

    /** Should {@code keyCode} hide the current panel. */
    public boolean isHideKeyForCurrentPanel(int keyCode) {
        if (isActive()) {
            SideFragment current =
                    (SideFragment) mFragmentManager.findFragmentById(R.id.side_fragment_container);
            return current != null && current.isHideKeyForThisPanel(keyCode);
        }
        return false;
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        //mAutoHideScheduler.onAccessibilityStateChanged(enabled);
    }
}
