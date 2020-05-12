package org.flval.discordwebview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.Objects;

public class DefThemeFragment extends PreferenceFragmentCompat {
    SharedPreferences sharedPreferences;
    Menu barMenu;
    SharedPreferences.Editor editor;
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settingsbar, menu);
        barMenu = menu;
        MenuItem item = barMenu.findItem(R.id.deletecss);
        item.setVisible(false);
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.defaulttheme, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        editor = sharedPreferences.edit();
        DropDownPreference dropDownPreference = findPreference("theme");
        assert dropDownPreference != null;
        SwitchPreference darkblack = findPreference("darkblack");
        SwitchPreference hidePaid = findPreference("hidepaid");
        assert darkblack != null;
        assert hidePaid != null;
        dropDownPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            editor.putString("mode", String.valueOf(newValue)).commit();
            editor.putBoolean("requireReload", true).commit();
            context = getContext();
            String darkmode = String.valueOf(newValue);
            switch (darkmode) {
                case "light":
                    darkblack.setEnabled(false);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":
                    darkblack.setEnabled(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "sysui":
                    darkblack.setEnabled(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            return true;
        });
        darkblack.setOnPreferenceChangeListener((preference, newValue) -> {
            editor.putBoolean("darkblack", (Boolean) newValue).commit();
            editor.putBoolean("requireReload", true).commit();
            return true;
        });
        hidePaid.setOnPreferenceChangeListener((preference, newValue) -> {
            editor.putBoolean("hidePaid", (Boolean) newValue).commit();
            editor.putBoolean("requireReload", true).commit();
            return true;
        });
    }
}
