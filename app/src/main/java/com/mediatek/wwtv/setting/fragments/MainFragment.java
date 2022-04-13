
package com.mediatek.wwtv.setting.fragments;
import android.os.Handler;
import android.os.Bundle;

import android.text.TextUtils;


import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;

import com.mediatek.wwtv.setting.LiveTvSetting;
import com.mediatek.wwtv.setting.preferences.PreferenceData;
import com.mediatek.wwtv.setting.preferences.DialogPreference;
import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.setting.view.PinDialog;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;
import com.mediatek.wwtv.setting.view.PinDialog.ResultListener;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
public class MainFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "MainFragment";

    private boolean mActive = false;
    private boolean mParentalControl = false;
    private boolean mFactoryMode = false;
    private PreferenceScreen mScreen;
    private TVContent mTv;
    private Handler mSignalHandler;
    private Handler mCAMHandler;

    public static MainFragment newInstance(boolean factoryMode) {
        return new MainFragment(factoryMode);
    }

    public MainFragment() {
        mFactoryMode = false;
    }

    public MainFragment(boolean factoryMode) {
        mFactoryMode = factoryMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate." + mFactoryMode);
        mSignalHandler = SettingsPreferenceScreen.getInstance(getContext(), getPreferenceManager()).mSignalHandler;
        mCAMHandler = SettingsPreferenceScreen.getInstance(getContext(), getPreferenceManager()).mCAMHandler;
        mTv = TVContent.getInstance(getContext());
        mTv.addSingleLevelCallBackListener(mSignalHandler);
        mTv.addCallBackListener(TvCallbackConst.MSG_CB_CI_MSG, mCAMHandler);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (mFactoryMode) {
            mScreen = SettingsPreferenceScreen.getInstance(
                    getContext(), getPreferenceManager()).getFactoryScreen();
        }
        else {
            SettingsPreferenceScreen screen = SettingsPreferenceScreen.getInstance(
                getContext(), getPreferenceManager());
            if(LiveTvSetting.isForChannelBootFromTVSettingPlus()){
                mScreen = screen.getChannelMainScreen();
            }else if(LiveTvSetting.isFor3rdCaptionsTVMenuOptionOrSaRegion()){
                mScreen = screen.getCaptionSetupScreen();
            }else if(LiveTvSetting.isForCAMScanFromTVMenuOption()){
                mScreen = screen.getCamScanOperatorScreen();
            }else{
                mScreen = screen.getMainScreen();
            }            
        }

        setPreferenceScreen(mScreen);

        for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
            Preference tempPre = mScreen.getPreference(i);

            if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_ENTER_PASSWORD)) {
                PinDialog dialog = (PinDialog) ((DialogPreference) tempPre).getDialog();
                dialog.setResultListener(listener);
            }
        }
    }

    @Override
    public void onStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart.");
        super.onStart();
    }

    @Override
    public void onResume() {
        PreferenceData data = PreferenceData.getInstance(getContext());

        data.setData(mScreen);
        data.resume();

        updateParentalControlList();

        mActive = true;

        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume.");
        super.onResume();
        if(TurnkeyUiMainActivity.getInstance() != null && LiveTvSetting.isBootFromLiveTV()) {
          TurnkeyUiMainActivity.getInstance().resetLayout();
        }
    }

    @Override
    public void onPause() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPause.");
        super.onPause();
        mActive = false;
        mParentalControl = false;
    }

    @Override
    public void onStop() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop.");
        mTv.removeCallBackListener(TvCallbackConst.MSG_CB_CI_MSG, mCAMHandler);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, " onDestroy()");
        mTv.removeSingleLevelCallBackListener(mSignalHandler);
        super.onDestroy();
    }

    public boolean getActive() {
        return mActive;
    }

    private void updateParentalControlList() {
        if (mScreen == null) {
            return;
        }

        for (int i = 0; i < mScreen.getPreferenceCount(); i++) {
            Preference tempPre = mScreen.getPreference(i);

            if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_ENTER_PASSWORD)) {
                tempPre.setVisible(!mParentalControl);
            }
            else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_CHANNEL_BLOCK)) {
                if (mTv.isCurrentSourceTv()
                        && CommonIntegration.getInstance().hasActiveChannel()) {
                    tempPre.setVisible(mParentalControl);
                } else {
                    tempPre.setVisible(false);
                }
            }
            else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_PROGRAM_BLOCK)) {
                tempPre.setVisible(mParentalControl);
            }
            else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_CHANNEL_SCHEDULE_BLOCK)) {
                tempPre.setVisible(mParentalControl);
            } else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_INPUT_BLOCK)) {
                tempPre.setVisible(mParentalControl);
            } else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_CHANGE_PASSWORD)) {
                tempPre.setVisible(mParentalControl);
            } else if (TextUtils.equals(tempPre.getKey(),
                    MenuConfigManager.PARENTAL_CLEAN_ALL)) {
                tempPre.setVisible(mParentalControl);
            }
        }
    }

    private ResultListener listener = new ResultListener() {
        @Override
        public void done(boolean success) {
            mParentalControl = success;
            updateParentalControlList();
        }
    };
}
