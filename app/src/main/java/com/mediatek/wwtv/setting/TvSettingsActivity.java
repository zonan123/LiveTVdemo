package com.mediatek.wwtv.setting;


import android.app.Fragment;


import android.content.Intent;

import android.os.Bundle;
//import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import androidx.annotation.Nullable;

import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;

import com.mediatek.wwtv.tvcenter.commonview.BaseActivity;

import com.android.tv.ui.sidepanel.SideFragmentManager;
import com.mediatek.tv.ui.pindialog.PinDialogFragment;
import com.mediatek.tv.ui.pindialog.PinDialogFragment.OnPinCheckedListener;

public abstract class TvSettingsActivity extends BaseActivity implements OnPinCheckedListener {
    private static final String TAG = "TvSettingsActivity";

    private SideFragmentManager mSideFragmentManager;

    private final int mContentResId = R.id.main_fragment_container;//android.R.id.content

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSideFragmentManager = new SideFragmentManager(this, null, null);
        if (savedInstanceState == null) {
            createFragment();
        }

    }

    @Override
    public void finish() {
        final Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
        if (isResumed() && fragment != null) {
            hide(1);
        } else {
            super.finish();
        }
    }

    private Fragment mFragment = null;

    protected String mActionId;

    public void hide() {
        if(mSideFragmentManager.isActive()) {
            mSideFragmentManager.setVisibility(View.GONE);
        }
        else {
            mFragment = getFragmentManager().findFragmentByTag(TAG);

            hide(0);
        }
    }

    private void hide(int type) {
        final Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
        if (isResumed() && fragment != null) {
            final ViewGroup root = (ViewGroup) findViewById(mContentResId);
            final Scene scene = new Scene(root);
            scene.setEnterAction(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commitNow();
                }
            });
            final Slide slide = new Slide(Gravity.END);
            if(root.getWidth() != 0) {
                slide.setSlideFraction(
                    getResources().getDimension(R.dimen.lb_settings_pane_width) / root.getWidth());
            }
            slide.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    getWindow().setDimAmount(0);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    if(type == 1) {
                        TvSettingsActivity.super.finish();
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionCancel");
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionResume");
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionResume");
                }
            });
            TransitionManager.go(scene, slide);
        }
    }

    public void show() {
        show(mFragment);
        mFragment = null;
    }

    public void show(Fragment fragment) {
        final ViewGroup root = (ViewGroup) findViewById(mContentResId);

        if (isResumed() && fragment != null) {
            final Scene scene = new Scene(root);
            scene.setEnterAction(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .remove(fragment)
                            .commitNow();
                }
            });
            final Slide slide = new Slide(Gravity.END);
            if(root.getWidth() != 0) {
                slide.setSlideFraction(
                    getResources().getDimension(R.dimen.lb_settings_pane_width) / root.getWidth());
            }
            slide.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    getWindow().setDimAmount(0);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                    createFragment();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionCancel");
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionPause");
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onTransitionResume");
                }
            });
            TransitionManager.go(scene, slide);
        }
        else {
            createFragment();
        }

        root.invalidate();
    }

    private void createFragment() {
        final ViewGroup root = (ViewGroup) findViewById(mContentResId);
        root.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        root.getViewTreeObserver().removeOnPreDrawListener(this);
                        final Scene scene = new Scene(root);
                        scene.setEnterAction(new Runnable() {
                            @Override
                            public void run() {
                                final Fragment fragment = createSettingsFragment();
                                if (fragment != null) {
                                    getFragmentManager().beginTransaction()
                                            .add(mContentResId, fragment,
                                                    TAG)
                                            .commitNow();
                                }
                            }
                        });

                        final Slide slide = new Slide(Gravity.END);
                        if(root.getWidth() != 0) {
                            slide.setSlideFraction(
                                getResources().getDimension(R.dimen.lb_settings_pane_width)
                                    / root.getWidth());
                        }
                        TransitionManager.go(scene, slide);

                        // Skip the current draw, there's nothing in it
                        return false;
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        mFragment = null;
    }

    @Override
    protected void onDestroy() {
      com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "now onDestroy");
      super.onDestroy();
    }

    public void updateFragment(String packageName,String activityName) {
        if(isPackageExist(packageName)){
            try{
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, activityName));
                startActivity(intent);
                this.finish();
            }catch (android.content.ActivityNotFoundException e){
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "ActivityNotFoundException = " + e.toString());
                //final Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
                //show(fragment);
            }catch (Exception e) {
                e.printStackTrace();
                com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "Exception = " + e.toString());
                //final Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
                //show(fragment);
            }

        }else{
            com.mediatek.wwtv.tvcenter.util.MtkLog.e(TAG, "not PackageExist");
            //final Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
            //show(fragment);
        }
    }

    private boolean isPackageExist(String packageName){
        try {
            ApplicationInfo app = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "packageInfo = " + app.toString());
            return true;
        }catch(PackageManager.NameNotFoundException e){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, e.toString());
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract Fragment createSettingsFragment();

    @Override
    public void onPinChecked(boolean checked, int type, String rating) {
        Log.d(TAG, "checked: " + checked + "type: " + type + "rating: " + rating );
        if(type == PinDialogFragment.PIN_DIALOG_TYPE_ENTER_PIN ) {
            if (checked) {
                mSideFragmentManager.show(
                    new com.android.tv.ui.sidepanel.
                        parentalcontrols.ParentalControlsFragment(),
                    true);
            }
            else {
                show();
            }
        }
        else if(type == PinDialogFragment.PIN_DIALOG_TYPE_NEW_PIN) {
            mSideFragmentManager.setVisibility(View.VISIBLE);
        }
        else if(type == PinDialogFragment.PIN_DIALOG_TYPE_START_SCAN){
            if (checked) {
                show();
            }
            else {
                if(!MenuConfigManager.CHANNEL_CHANNEL_SOURCES.equals(mActionId)){
                    show();
                }
                setActionId(null);
            }
        }
    }

    public SideFragmentManager getSideFragmentManager() {
        return mSideFragmentManager;
    }

    public void setActionId(String actionId) {
        mActionId = actionId;
    }

    @Override
    public void onBackPressed() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG,"onBackPressed mActionId="+mActionId);
        if(MenuConfigManager.TV_EU_CHANNEL.equals(mActionId) || MenuConfigManager.TV_CHANNEL.equals(mActionId)){
            setActionId(null);
            show(getFragmentManager().findFragmentByTag(TAG));
                return;
        }
        if(MenuConfigManager.DVBS_SAT_RE_SCAN.equals(mActionId) || MenuConfigManager.TV_CHANNEL_SCAN_DVBC.equals(mActionId)){
            setActionId(MenuConfigManager.TV_EU_CHANNEL);
            show(getFragmentManager().findFragmentByTag(TAG));
            return;
        }
        super.onBackPressed();
    }
}
