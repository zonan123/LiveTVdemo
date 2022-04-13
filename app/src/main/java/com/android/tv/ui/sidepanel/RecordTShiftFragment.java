package com.android.tv.ui.sidepanel;

import android.content.Context;
import android.os.Bundle;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.PreferenceScreen;
//import android.support.v17.preference.LeanbackPreferenceFragment;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;

import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.R;

public class RecordTShiftFragment extends LeanbackPreferenceFragment{
    //private static final String TAG = "RecordTShiftFragment";

    private MenuConfigManager mConfigManager;
    public static RecordTShiftFragment newInstance() {
        return new RecordTShiftFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mConfigManager = MenuConfigManager.getInstance(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferenceScreen(getRecordTShiftScreen());
    }

    private PreferenceScreen getRecordTShiftScreen(){
        final Context themedContext = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen =
                getPreferenceManager().createPreferenceScreen(themedContext);
        preferenceScreen.setTitle(R.string.menu_setup_record_setting);
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);       

        //Time Shift Mode        
        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.SETUP_SHIFTING_MODE,
                R.string.menu_setup_time_shifting_mode,
                mConfigManager.getDefault(MenuConfigManager.SETUP_SHIFTING_MODE)==1 ? true :false));

        return preferenceScreen ;
    }
}
