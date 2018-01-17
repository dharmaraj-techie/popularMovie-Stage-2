package com.example.dharmaraj.popularmovie;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;

/**
 * Created by Dharmaraj on 07-05-2017.
 */

public  class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen  preferenceScreen  = getPreferenceScreen();
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();
        //register the change listener so that some thing change we can change the summery
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        //get the number of preference available
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0 ; i<count ; i++){
            Preference p = preferenceScreen.getPreference(i);
            if(p instanceof ListPreference) {
                String value =  sharedPreferences.getString(p.getKey(),"");
                setPreferenceSummary(p,value);
            }
        }
    }

    @Override
    public void onDestroyView() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    /**
     * set the preference summary to the ListPreference
     * @param preference
     * @param string
     */
    void setPreferenceSummary(Preference preference, String string){
        //cast the preference to list preference
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(string);
        if(index>=0) preference.setSummary(listPreference.getEntries()[index]);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        String value = sharedPreferences.getString(key,"");
        if(preference instanceof ListPreference) {
             setPreferenceSummary(preference,value);
        }
    }
}
