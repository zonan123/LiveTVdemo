package com.mediatek.wwtv.setting.view;

import android.content.Context;
import android.content.Intent;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;

//import android.support.v7.preference.Preference.OnPreferenceClickListener;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;


import com.mediatek.wwtv.setting.preferences.PreferenceData;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

public final class FacPreset implements OnPreferenceClickListener{
    private static final String TAG = "FacPreset";

    private static FacPreset mInstance = null;

    private Context mContext;
    private PreferenceData mPrefData;

    private FacPreset(Context context) {
        mContext = context;
        mPrefData = PreferenceData.getInstance(mContext);
    }

    public static synchronized FacPreset getInstance(Context context) {
        if(null == mInstance) {
            mInstance = new FacPreset(context);
        }

        return mInstance;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "onPreferenceClick, " + preference);

        if(preference.getKey().equals(
            mPrefData.mConfigManager.FACTORY_PRESET_CH_DUMP)) {
            mPrefData.mTV.setConfigValue(
                MtkTvConfigTypeBase.CFG_MISC_PRE_CH_DUMP_CH_INFO_2USB, 0);
        }
        else if(preference.getKey().equals(
            mPrefData.mConfigManager.FACTORY_PRESET_CH_PRINT)) {
            mPrefData.mTV.setConfigValue(
                MtkTvConfigTypeBase.CFG_MISC_PRE_CH_DUMP_CH_INFO_2TERM, 0);
        }
        else if(preference.getKey().equals(
            mPrefData.mConfigManager.FACTORY_PRESET_CH_RESTORE)) {
            mPrefData.mTV.setConfigValue(
                MtkTvConfigTypeBase.CFG_MISC_PRE_CH_LOAD_PRESET_CH, 0);

            Intent intent = new Intent("com.mediatek.timeshift.mode.off");
            mContext.sendBroadcast(intent);
        }
        return true;
    }
}

