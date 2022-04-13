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

package com.android.tv.menu;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import androidx.annotation.IntDef;
import androidx.leanback.widget.HorizontalGridView;
import com.android.tv.common.util.DurationTimer;
import com.android.tv.menu.MenuRowFactory.TvOptionsRow;
import com.android.tv.menu.MenuRowFactory.RecordRow;
import com.android.tv.ui.hideable.AutoHideScheduler;
import com.android.tv.util.ViewCache;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.util.MarketRegionInfo;
import com.mediatek.wwtv.tvcenter.util.DataSeparaterUtil;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

/** A class which controls the menu. */
public class Menu implements AccessibilityStateChangeListener {
    private static final String TAG = "Menu";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        REASON_NONE,
        REASON_GUIDE,
        REASON_PLAY_CONTROLS_PLAY,
        REASON_PLAY_CONTROLS_PAUSE,
        REASON_PLAY_CONTROLS_PLAY_PAUSE,
        REASON_PLAY_CONTROLS_REWIND,
        REASON_PLAY_CONTROLS_FAST_FORWARD,
        REASON_PLAY_CONTROLS_JUMP_TO_PREVIOUS,
        REASON_PLAY_CONTROLS_JUMP_TO_NEXT
    })
    public @interface MenuShowReason {}

    public static final int REASON_NONE = 0;
    public static final int REASON_GUIDE = 1;
    public static final int REASON_PLAY_CONTROLS_PLAY = 2;
    public static final int REASON_PLAY_CONTROLS_PAUSE = 3;
    public static final int REASON_PLAY_CONTROLS_PLAY_PAUSE = 4;
    public static final int REASON_PLAY_CONTROLS_REWIND = 5;
    public static final int REASON_PLAY_CONTROLS_FAST_FORWARD = 6;
    public static final int REASON_PLAY_CONTROLS_JUMP_TO_PREVIOUS = 7;
    public static final int REASON_PLAY_CONTROLS_JUMP_TO_NEXT = 8;

    public static final int REASON_LAUNCH_TV_OPTIONS = 9;
    public static final int REASON_LAUNCH_TV_RECORD = 10;

    private final Context mContext;
    private final IMenuView mMenuView;
    private final long mShowDurationMillis;
    private final OnMenuVisibilityChangeListener mOnMenuVisibilityChangeListener;
    private final Animator mShowAnimator;
    private final Animator mHideAnimator;
    private final AutoHideScheduler mAutoHideScheduler;
    private final DurationTimer mVisibleTimer = new DurationTimer();
    private boolean mAnimationDisabledForTest;
    private final List<MenuRow> mMenuRows = new ArrayList<>();
    private OnAutoHideListener mOnAutoHideListener;

    private static final Map<Integer, Integer> PRELOAD_VIEW_IDS = new HashMap<>();

    static {
        PRELOAD_VIEW_IDS.put(R.layout.menu_card_guide, 1);
        PRELOAD_VIEW_IDS.put(R.layout.menu_card_setup, 1);
        //PRELOAD_VIEW_IDS.put(R.layout.menu_card_dvr, 1);
        PRELOAD_VIEW_IDS.put(R.layout.menu_card_app_link, 1);
        PRELOAD_VIEW_IDS.put(R.layout.menu_card_channel, ChannelsRow.MAX_COUNT_FOR_RECENT_CHANNELS);
        PRELOAD_VIEW_IDS.put(R.layout.menu_card_action, 7);
    }

    private static final List<String> sRowIdListForReason = new ArrayList<>();

    static {
        sRowIdListForReason.add(null); // REASON_NONE
        sRowIdListForReason.add(ChannelsRow.ID); // REASON_GUIDE
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_PLAY
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_PAUSE
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_PLAY_PAUSE
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_REWIND
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_FAST_FORWARD
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_JUMP_TO_PREVIOUS
        sRowIdListForReason.add(null);//PlayControlsRow.ID); // REASON_PLAY_CONTROLS_JUMP_TO_NEXT

        sRowIdListForReason.add(TvOptionsRow.ID); // REASON_LAUNCH_TV_OPTIONS
        sRowIdListForReason.add(RecordRow.ID); // REASON_LAUNCH_TV_RECORD
    }

    public Menu(
            Context context,
            IMenuView menuView,
            MenuRowFactory menuRowFactory,
            OnMenuVisibilityChangeListener onMenuVisibilityChangeListener,OnAutoHideListener onAutoHideListener) {
        mContext = context;
        mMenuView = menuView;
        Resources res = context.getResources();
        mShowDurationMillis = res.getInteger(R.integer.menu_show_duration);
        mOnMenuVisibilityChangeListener = onMenuVisibilityChangeListener;
        mShowAnimator = AnimatorInflater.loadAnimator(context, R.animator.menu_enter);
        mShowAnimator.setTarget(mMenuView);
        mHideAnimator = AnimatorInflater.loadAnimator(context, R.animator.menu_exit);
        mHideAnimator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideInternal();
                    }
                });
        mHideAnimator.setTarget(mMenuView);
        // Build menu rows
        //addMenuRow(menuRowFactory.createMenuRow(this, PlayControlsRow.class));
        addMenuRow(menuRowFactory.createMenuRow(this, ChannelsRow.class));
        //addMenuRow(menuRowFactory.createMenuRow(this, PartnerRow.class));
        addMenuRow(menuRowFactory.createMenuRow(this, TvOptionsRow.class));

        //Record Setting
        if(!CommonIntegration.isCNRegion()){
            if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_SET_RECORD_SETTING_SUPP)
                    && DataSeparaterUtil.getInstance() != null
                    && DataSeparaterUtil.getInstance().isSupportPvr()){
                addMenuRow(menuRowFactory.createMenuRow(this, RecordRow.class));
            }
            else if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_TIMESHIFT)) {
                addMenuRow(menuRowFactory.createMenuRow(this, RecordRow.class));
            }
        }

        mMenuView.setMenuRows(mMenuRows);
        mOnAutoHideListener=onAutoHideListener;
        mAutoHideScheduler = new AutoHideScheduler(context, () -> autoHide());
    }

    private void autoHide(){
        if(mOnAutoHideListener!=null) {
            mOnAutoHideListener.onAutoHide();
        }
    }
/*
    private static final String SCREEN_NAME = "Menu";

    private final Tracker mTracker;

    private final MenuUpdater mMenuUpdater;


    @VisibleForTesting
    Menu(
            Context context,
            IMenuView menuView,
            MenuRowFactory menuRowFactory,
            OnMenuVisibilityChangeListener onMenuVisibilityChangeListener) {
        this(context, null, null, menuView, menuRowFactory, onMenuVisibilityChangeListener);
    }

    public void setChannelTuner(ChannelTuner channelTuner) {
        mMenuUpdater.setChannelTuner(channelTuner);
    }
*/
    public void preloadItemViews() {
        HorizontalGridView fakeParent = new HorizontalGridView(mContext);
        for (int id : PRELOAD_VIEW_IDS.keySet()) {
            ViewCache.getInstance().putView(mContext, id, fakeParent, PRELOAD_VIEW_IDS.get(id));
        }
    }

    public boolean update() {
        Log.d(TAG, "update main menu");
        return mMenuView.update(isActive());
    }

    public boolean update(String rowId) {
        Log.d(TAG, "update main menu");
        return mMenuView.update(rowId, isActive());
    }
    
    public void updateLanguage(){
    	mMenuView.updateLanguage();
    }

    public void onRecentChannelsChanged() {
        Log.d(TAG, "onRecentChannelsChanged");
        for (MenuRow row : mMenuRows) {
            row.onRecentChannelsChanged();
        }
    }

    public void onStreamInfoChanged() {
        Log.d(TAG, "update options row in main menu");
        //mMenuUpdater.onStreamInfoChanged();
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        mAutoHideScheduler.onAccessibilityStateChanged(enabled);
    }

    public boolean isActive() {
        return mMenuView.isVisible() && !mHideAnimator.isStarted();
    }

    /**
     * Shows the main menu.
     *
     * @param reason A reason why this is called. See {@link MenuShowReason}
     */
    public void show(@MenuShowReason int reason) {
        Log.d(TAG, "menu show reason:" + reason);
        //mTracker.sendShowMenu();
        mVisibleTimer.start();
        //mTracker.sendScreenView(SCREEN_NAME);
        if (mHideAnimator.isStarted()) {
            mHideAnimator.end();
        }
        if (mOnMenuVisibilityChangeListener != null) {
            mOnMenuVisibilityChangeListener.onMenuVisibilityChange(true);
        }
        String rowIdToSelect = sRowIdListForReason.get(reason);
        mMenuView.onShow(
                reason,
                rowIdToSelect,
                mAnimationDisabledForTest
                        ? null
                        : () -> {
                            if (isActive()) {
                                mShowAnimator.start();
                            }
                        });
        scheduleHide();
    }

    /** Closes the menu. */
    public void hide(boolean withAnimation) {
        Log.d(TAG, "menu hide :" + withAnimation);
        if (mShowAnimator.isStarted()) {
            mShowAnimator.cancel();
        }
        if (!isActive()) {
            return;
        }
        if (mAnimationDisabledForTest) {
            withAnimation = false;
        }
        mAutoHideScheduler.cancel();
        if (withAnimation) {
            if (!mHideAnimator.isStarted()) {
                mHideAnimator.start();
            }
        } else if (mHideAnimator.isStarted()) {
            // mMenuView.onHide() is called in AnimatorListener.
            mHideAnimator.end();
        } else {
            hideInternal();
        }
    }

    private void addMenuRow(MenuRow row) {
        if (row != null) {
            mMenuRows.add(row);
        }
    }

    public void release() {
        //mMenuUpdater.release();
        for (MenuRow row : mMenuRows) {
            row.release();
        }
        mAutoHideScheduler.cancel();
    }

    /** Schedules to hide the menu in some seconds. */
    public void scheduleHide() {
        mAutoHideScheduler.schedule(mShowDurationMillis);
    }

    public abstract static class OnMenuVisibilityChangeListener {
        public abstract void onMenuVisibilityChange(boolean visible);
    }

    public interface OnAutoHideListener {
        void onAutoHide();
    }

    private void hideInternal() {
        Log.d(TAG, "menu hideInternal");
        mMenuView.onHide();
        //mTracker.sendHideMenu(mVisibleTimer.reset());
        if (mOnMenuVisibilityChangeListener != null) {
            mOnMenuVisibilityChangeListener.onMenuVisibilityChange(false);
        }
    }

    public void setKeepVisible(boolean keepVisible) {
    	boolean mKeepVisible = false;
        mKeepVisible = keepVisible;
        if (mKeepVisible) {
            mAutoHideScheduler.cancel();
        } else if (isActive()) {
            scheduleHide();
        }
    }
}