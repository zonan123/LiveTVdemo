package com.mediatek.wwtv.setting;

import android.app.Fragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
//import android.support.v14.preference.PreferenceDialogFragment;
//import android.support.v14.preference.PreferenceFragment;
//import android.support.v17.preference.LeanbackSettingsFragment;
//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragment;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;

/**
 * Base class for settings fragments. Handles launching fragments and dialogs in a reasonably
 * generic way. Subclasses should only override onPreferenceStartInitialScreen.
 */

public abstract class BaseSettingsFragment extends LeanbackSettingsFragment {
    @Override
    public final boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        final Fragment f =
                Fragment.instantiate(getActivity(), pref.getFragment(), pref.getExtras());
        f.setTargetFragment(caller, 0);
        if (f instanceof PreferenceFragment || f instanceof PreferenceDialogFragment) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
    }

    @Override
    public final boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        return false;
    }
}
