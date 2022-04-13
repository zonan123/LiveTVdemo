package com.android.tv.ui.sidepanel;

import android.os.Bundle;
//import java.util.ArrayList;
//import java.util.List;
import com.mediatek.wwtv.setting.util.LanguageUtil;
import com.mediatek.wwtv.setting.util.MenuConfigManager;
import com.mediatek.wwtv.setting.util.TVContent;
import com.mediatek.wwtv.tvcenter.R;
import com.mediatek.wwtv.tvcenter.TvSingletons;
import com.mediatek.wwtv.tvcenter.nav.util.SundryImplement;
import com.mediatek.wwtv.tvcenter.util.CommonIntegration;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.PreferenceScreen;
import android.content.Context;
import androidx.preference.Preference;
//import com.mediatek.wwtv.setting.preferences.PreferenceUtil;
import src.com.android.tv.ui.sidepanel.RadioPreference;
import com.mediatek.wwtv.tvcenter.util.InstrumentationHandler;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MultiAudioFragment extends LeanbackPreferenceFragment {

    private static final String TAG = "MultiAudioFragment";

    private MenuConfigManager mConfigManager;
    private String[] multiAudio;
    private String key = MenuConfigManager.TV_MTS_MODE;
    private int mInitialSelectedPosition = -1;
    private Context mContext;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState,String rootKey) {
        mContext = getPreferenceManager().getContext();
        mConfigManager = MenuConfigManager.getInstance(getActivity());
        CommonIntegration ci = TvSingletons.getSingletons().getCommonIntegration();
        LanguageUtil mOsdLanguage = new LanguageUtil(getActivity().getApplicationContext());
        multiAudio = getActivity().getResources().getStringArray(R.array.menu_tv_audio_channel_mts_array);
        if(!(CommonIntegration.getInstance().isCurrentSourceATV()
                || !ci.isCurrentSourceHasSignal())) {
            multiAudio = getActivity().getResources().getStringArray(R.array.menu_tv_audio_language_array_US_value);
            key = MenuConfigManager.TV_AUDIO_LANGUAGE;
            mInitialSelectedPosition = mOsdLanguage.getAudioLanguage(key);

            List<String> mAudioList = new ArrayList<>();
            mAudioList = subtitleAudioLanguageTransform("Audio",multiAudio);

            multiAudio = new String[mAudioList.size()];
            for (int i=0;i<mAudioList.size();i++){
                multiAudio[i] = mAudioList.get(i);
            }

        } else {
            SundryImplement sundryIm = SundryImplement.getInstanceNavSundryImplement(getActivity());
            multiAudio = sundryIm.getAllMtsModes();
            mInitialSelectedPosition = getIndexOfMtsMode(multiAudio, sundryIm.getMtsModeString(TVContent.getInstance(getActivity()).getConfigValue(MenuConfigManager.TV_MTS_MODE)));
            //mInitialSelectedPosition = mConfigManager.getDefault(key);
        }

        final PreferenceScreen screen =
                getPreferenceManager().createPreferenceScreen(mContext);
        screen.setTitle(R.string.menu_channel_multi_audio);
        Preference activePref = null;
        for(int i=0;i<multiAudio.length;i++){
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiAudioFragment", "-------"+multiAudio[i]+"------"+i);
            final RadioPreference radioPreference = new RadioPreference(mContext);
            radioPreference.setKey(multiAudio[i]);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(multiAudio[i]);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);

            if (i == mInitialSelectedPosition) {
                radioPreference.setChecked(true);
                activePref = radioPreference;
            }

            screen.addPreference(radioPreference);
        }

        if (activePref != null && savedInstanceState == null) {
            scrollToPreference(activePref);
        }

        setPreferenceScreen(screen);
    }
    
    private int getIndexOfMtsMode(String[] modes,String mode){
        for(int i = 0; i<modes.length; i++){
            if(mode.equalsIgnoreCase(modes[i])){
                return i;
            }
        }
        return 0;
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String prefKey = preference.getKey();
        com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiAudioFragment", "onPreferenceTreeClick,prefKey=="+prefKey);
        if (preference instanceof RadioPreference && prefKey != null) {


            onSaveValue(preference, prefKey);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void onSaveValue(Preference preference, String prefKey) {
        final RadioPreference radioPreference = (RadioPreference) preference;
        radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
        if (!radioPreference.isChecked()) {
            radioPreference.setChecked(true);
        }

        for (int i = 0; i < multiAudio.length; i++) {
            com.mediatek.wwtv.tvcenter.util.MtkLog.d("MultiAudioFragment", "mEntries[i]=="+multiAudio[i]);
            if (TextUtils.equals(multiAudio[i], prefKey)){
                mConfigManager.setValue(key, i);
                break;
            }
        }
        InstrumentationHandler.getInstance().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);

    }

    /**
     *
     * @param tag mark log
     * @param list local language list
     * @return after transform
     */
    private List<String> subtitleAudioLanguageTransform(String tag, String[] list){
        List<String> mSubtitleAudioUnormalList = new ArrayList<String>();
        List<String> mSubtitleAudioUnormalListstr = new ArrayList<String>();
        List<String> mSubAudioList = new ArrayList<>();

        mSubtitleAudioUnormalList = new LinkedList<String>(
                Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array)));
        mSubtitleAudioUnormalListstr = new LinkedList<String>(
                Arrays.asList(mContext.getResources().getStringArray(R.array.menu_tv_subtitle_audio_language_unnormal_array_value)));


        for (String s : list) {
            String value = s.trim().toLowerCase(Locale.getDefault());
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_value:" + value);

            if (TVContent.getInstance(mContext).isEspCountry() && "und".equalsIgnoreCase(value)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "esp--> und");
                value = mContext.getResources().getString(R.string.menu_arrays_other);
            } else if (TVContent.getInstance(mContext).isEspCountry() && "qaa".equalsIgnoreCase(value)) {
                com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, "esp--> qaa");
                value = mContext.getResources().getString(R.string.menu_arrays_Original_Version);
            } else if ("msa".equals(value)) {
                value = mContext.getResources().getString(R.string.menu_arrays_Bahasa_Melayu);
            } else {
                Locale locale = new Locale(value);
                String localname = locale.getDisplayLanguage();
                if (!localname.equals(value)) {
                    value = localname;
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_local:" + value);
                } else {
                    int mIndex = -1;
                    for (int mUNSubtitle = 0; mUNSubtitle < mSubtitleAudioUnormalListstr.size(); mUNSubtitle++) {
                        if (value.contains(mSubtitleAudioUnormalListstr.get(mUNSubtitle))) {
                            mIndex = mUNSubtitle;
                            break;
                        }
                    }

                    if (mIndex >= 0) {
                        value = mSubtitleAudioUnormalList.get(mIndex);
                    }
                    com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_unnormal:" + value);
                }
            }
            com.mediatek.wwtv.tvcenter.util.MtkLog.d(TAG, tag + "_last:" + value);
            mSubAudioList.add(value);
        }
        return mSubAudioList;
    }
}
