package org.flval.discordwebview;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class CSSFragment extends PreferenceFragmentCompat {

    Boolean deletemode = false;
    Menu barMenu;
    MenuItem deleteButton;
    PreferenceCategory cat;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settingsbar, menu);
        barMenu = menu;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            String uri = data.getDataString();
            Uri filepath = data.getData();
            final int flags = data.getFlags() & (FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            ContentResolver resolver = getContext().getContentResolver();
            if(Build.VERSION.SDK_INT < 19) {
                getContext().getApplicationContext().grantUriPermission("org.flval.discordwebview", filepath, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                resolver.takePersistableUriPermission(filepath, flags);
            }
            PreferenceScreen thisscreen = getPreferenceScreen();
            PreferenceCategory thiscategory = (PreferenceCategory) findPreference("cssfiles");
            CheckBoxPreference pref = new CheckBoxPreference(getContext());
            pref.setTitle(filepath.toString());
            pref.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString() == "true") {
                        pref.setChecked(true);
                    } else {
                        pref.setChecked(false);
                    }
                    editor.putString(String.valueOf("PATH" + preference), newValue.toString()).commit();
                    return true;
                }
            });
            editor.putString(String.valueOf("PATH" + filepath), "true").commit();
            thiscategory.addPreference(pref);
            pref.setChecked(true);
        }
    }
    List<String> paths = new ArrayList<>();
    private void setmode() {
        SwitchPreference enabled = (SwitchPreference) findPreference("enabled");
        Preference addfile = (Preference) findPreference("addfile");
        cat = (PreferenceCategory) findPreference("cssfiles");
        int listCount = cat.getPreferenceCount();
        if (!deletemode) {
            for (int i = 1; i<listCount; i++) {
                CheckBoxPreference currentPref = (CheckBoxPreference) cat.getPreference(i);
                paths.add(String.valueOf(currentPref.isChecked()));
                currentPref.setChecked(false);
            }
            enabled.setVisible(false);
            addfile.setVisible(false);
            deletemode = true;
            deleteButton.setIcon(R.drawable.ic_baseline_check_24);
        } else {
            List<String> isRemoved = new ArrayList<>();
            for (int i = 1; i<listCount; i++) {
                CheckBoxPreference currentPref = (CheckBoxPreference) cat.getPreference(i);
                if (currentPref.isChecked() == true) {
                    cat.removePreference(currentPref);
                    editor.remove("PATH" + currentPref.getTitle().toString()).commit();
                    listCount--;
                    i--;
                } else {
                    if (paths.get(i - 1).equals("true")) {
                        currentPref.setChecked(true);
                    } else {
                        currentPref.setChecked(false);
                    }
                }
            }
            enabled.setVisible(true);
            addfile.setVisible(true);
            deletemode = false;
            deleteButton.setIcon(R.drawable.ic_baseline_delete_24);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        deleteButton = barMenu.getItem(0);
        switch (item.getItemId()) {
            case R.id.deletecss:
                setmode();
                return true;
        }
        return true;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.csspref, rootKey);
        sharedPreferences = (SharedPreferences) getPreferenceManager().getSharedPreferences();
        editor = sharedPreferences.edit();
        SwitchPreference enabled = (SwitchPreference) findPreference("enabled");
        enabled.setChecked(true);
        enabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putBoolean("CSSEnabled", Boolean.valueOf(newValue.toString()));
                return true;
            }
        });
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Map<String, ?> saveddata = sharedPreferences.getAll();
        PreferenceCategory thiscat = (PreferenceCategory) findPreference("cssfiles");
        for(Map.Entry<String, ?> entry : saveddata.entrySet()) {
            if (entry.toString().startsWith("PATH")) {
                CheckBoxPreference pref = new CheckBoxPreference(getContext());
                String realentry = entry.toString().substring(4);
                String[] entryParts = realentry.split("=");
                pref.setTitle(entryParts[0]);
                if (entryParts[1].toString().equals("true")) {
                    pref.setChecked(true);
                } else {
                    pref.setChecked(false);
                }
                pref.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue.toString() == "true") {
                            pref.setChecked(true);
                        } else {
                            pref.setChecked(false);
                        }
                        editor.putString(String.valueOf("PATH" + preference), newValue.toString()).apply();
                        return true;
                    }
                });
                thiscat.addPreference(pref);
            }
        }
        enabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PreferenceCategory cssfiles = (PreferenceCategory) findPreference("cssfiles");
                if (newValue.toString() == "true") {
                    cssfiles.setEnabled(true);
                } else {
                    cssfiles.setEnabled(false);
                }
                return true;
            }
        });
        Preference addfile = (Preference) findPreference("addfile");
        addfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent file = new Intent(ACTION_OPEN_DOCUMENT);
                file.setType("text/css");
                file.addCategory(Intent.CATEGORY_OPENABLE);
                file.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(file, 8778);
                return true;
            }
        });
    }
}