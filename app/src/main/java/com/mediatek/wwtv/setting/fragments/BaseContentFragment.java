package com.mediatek.wwtv.setting.fragments;


import android.os.Bundle;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.PreferenceScreen;

//import android.support.v17.preference.LeanbackPreferenceFragment;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;
//import android.support.v7.preference.ListPreference;

import com.mediatek.wwtv.setting.base.scan.model.ScanContent;
import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.preferences.SettingsPreferenceScreen;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.preferences.PreferenceData;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import com.mediatek.wwtv.tvcenter.util.SaveValue;

public class BaseContentFragment extends LeanbackPreferenceFragment {
    private static final String TAG = "BaseContentFragment";

    private PreferenceScreen mScreen;
    private SettingsPreferenceScreen mSettingSPreferenceScreen;

    private String parent;

    public static BaseContentFragment newInstance() {
        return new BaseContentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreate.");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle data = getArguments();
        
        parent = data.getString(PreferenceUtil.PARENT_PREFERENCE_ID);
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onCreatePreferences, " + parent);
        //TAG = TAG_BASE + parent;
        mSettingSPreferenceScreen=SettingsPreferenceScreen.getInstance(
                getContext(), getPreferenceManager());
        mScreen = mSettingSPreferenceScreen.getSubScreen(parent);

        setPreferenceScreen(mScreen);

        if (CommonIntegration.isTVSourceSeparation()
                && TVContent.getInstance(getContext()).isCurrentSourceDTV()) {
            if (MenuConfigManager.SUBTITLE_GROUP.equalsIgnoreCase(parent)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume, set subtitle focus item to Digital Subtitle");

                scrollToPreference(MenuConfigManager.DIGITAL_SUBTITLE_LANG_ENABLE);
            }
        }
    }

    @Override
    public void onStart() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStart.");
        int cur = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
        if(cur == 5 || cur == 6) {
        	MenuConfigManager.PICTURE_MODE_dOVI = true;
        } else {
        	MenuConfigManager.PICTURE_MODE_dOVI = false;
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onResume.");
        PreferenceUtil.isFromSubtitleTrackclick = false;
        PreferenceData data = PreferenceData.getInstance(getContext());

        data.setData(mScreen);
        data.resume();
        
        if(CommonIntegration.getInstance().getTunerMode() > 1 
                && MenuConfigManager.TV_EU_CHANNEL.equals(parent) &&
                mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD) != null){
            if(ScanContent.isPreferedSat()){
                mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setEnabled(false);
                mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setSelectable(false);
            }else {
                int size = ScanContent.getDVBSDisableSatellites(getContext()).size();
                if (size == 0) {
                    scrollToPreference(MenuConfigManager.DVBS_SAT_RE_SCAN);
                    mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setEnabled(false);
                    mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setSelectable(false);
                } else {
                    mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setEnabled(true);
                    mScreen.findPreference(MenuConfigManager.DVBS_SAT_ADD).setSelectable(true);
                }
            }
        }

        if (CommonIntegration.getInstance().getTunerMode() > 1 &&
                MenuConfigManager.DVBS_RESCAN_MORE.equals(parent)) {
            int currentType = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.DVBS_SAT_ATENNA_TYPE);
            switch (currentType){
                case MenuConfigManager.DVBS_ACFG_SINGLE:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_SINGLE);
                    break;
                case MenuConfigManager.DVBS_ACFG_DISEQC10:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_DISEQC10);
                    break;
                case MenuConfigManager.DVBS_ACFG_DISEQC11:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_DISEQC11);
                    break;
                case MenuConfigManager.DVBS_ACFG_DISEQC12:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_DISEQC12);
                    break;
                case MenuConfigManager.DVBS_ACFG_UNICABLE1:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_UNICABLE1);
                    break;
                case MenuConfigManager.DVBS_ACFG_UNICABLE2:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_UNICABLE2);
                    break;
                default:
                    scrollToPreference(MenuConfigManager.DVBS_RESCAN_SINGLE);
                    break;
            }

        }
		
		if(ScanContent.isSatOpFastScan() && MenuConfigManager.DVBS_SAT_UPDATE_SCAN.equals(parent)){
            String subOptName = SaveValue.readWorldStringValue(getContext(), "FAST_SCAN_SELECTED_OPT");
		    if(subOptName != null && !subOptName.isEmpty()){
                scrollToPreference(subOptName);
            }
        }
		
        super.onResume();
    }

    @Override
    public void onPause() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPause.");
        PreferenceData data = PreferenceData.getInstance(getContext());
        data.pause();
        super.onPause();
    }

    @Override
    public void onStop() {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onStop.");
        MenuConfigManager.PICTURE_MODE_dOVI = false;
        if(mSettingSPreferenceScreen != null && mSettingSPreferenceScreen.mMenuSystemInfo != null){
            mSettingSPreferenceScreen.mMenuSystemInfo.stopRefresh();
        }
        super.onStop();
    }
}

