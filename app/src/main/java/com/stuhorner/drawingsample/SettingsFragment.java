package com.stuhorner.drawingsample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));

        Preference button = findPreference(getString(R.string.logout));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                User.getInstance().newInstance(getActivity().getApplicationContext());
                SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //set first launch
                editor.putBoolean("isFirstLaunch", true);

                //delete gallery files
                String dir = sharedPreferences.getString(getString(R.string.directory), null);
                if (dir != null) {
                    File directory = new File(dir);
                    for (File file : directory.listFiles()) {
                        file.delete();
                    }
                }
                editor.clear();

                //delete profile picture files
                SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                pref.clear();
                pref.apply();

                editor.apply();
                Intent intent = new Intent(getActivity(), FirstLaunchActivity.class);
                startActivity(intent);
                pref.apply();
                return true;
            }
        });
        return view;
    }

}
