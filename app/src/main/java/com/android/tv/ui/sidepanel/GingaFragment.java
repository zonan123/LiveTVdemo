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
import com.mediatek.wwtv.setting.util.TVContent;

public class GingaFragment extends LeanbackPreferenceFragment{
    //private static final String TAG = "GingaFragment";
	private TVContent mTV;

    //private MenuConfigManager mConfigManager;
    public static GingaFragment newInstance() {
        return new GingaFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //mConfigManager = MenuConfigManager.getInstance(getActivity());
		mTV = TVContent.getInstance(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferenceScreen(getGingaScreen());
    }

    private PreferenceScreen getGingaScreen(){
        final Context themedContext = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen =
                getPreferenceManager().createPreferenceScreen(themedContext);
        preferenceScreen.setTitle(R.string.menu_setup_ginga_setup);
        PreferenceUtil util = PreferenceUtil.getInstance(themedContext);       

        // Ginga Enable

        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.GINGA_ENABLE,
                R.string.menu_setup_ginga_enable,
                mTV.getConfigValue(MenuConfigManager.GINGA_ENABLE) == 0 ? false : true));

        //Auto Start Application

        preferenceScreen.addPreference(util.createSwitchPreference(
                MenuConfigManager.AUTO_START_APPLICATION,
                R.string.menu_setup_ginga_auto_start_app,
                mTV.getConfigValue(MenuConfigManager.AUTO_START_APPLICATION)==0? false :true));

        return preferenceScreen;
    }
}
