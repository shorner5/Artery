package com.stuhorner.drawingsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));

        Preference logout = findPreference(getString(R.string.logout));
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MyUser.getInstance().newInstance(getActivity().getApplicationContext());
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                //delete gallery files
                String dir = sharedPreferences.getString(getString(R.string.directory), null);

                if (dir != null) {
                    File directory = new File(dir);
                    for (File file : directory.listFiles()) {
                        file.delete();
                    }
                }
                editor.clear();
                editor.apply();

                MyLocationListener.hasLocation = false;

                Intent intent = new Intent(getActivity(), FirstLaunchActivity.class);
                startActivity(intent);
                return true;
            }
        });
        Preference clearSeen = findPreference(getString(R.string.clear_seen));
        clearSeen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MainActivity.rootRef.child("users").child(MyUser.getInstance().getUID()).child("swiped").setValue(null);
                Snackbar.make(view, "Reset Swipes. This does not affect your matches.", Snackbar.LENGTH_SHORT).show();
                MyUser.getInstance().clearSwiped();
                return false;
            }
        });
        return view;
    }

}
