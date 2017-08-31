package com.example.android.simpleshoppinglist;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

/**
 * Created by Oleg on 31.08.2017.
 */

public class PrefFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.list_preferences,rootKey);
    }
}
