package org.flval.discordwebview;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settingsbar, menu);
        barMenu = menu;
        MenuItem item = barMenu.findItem(R.id.deletecss);
        item.setVisible(true);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            assert data != null;
            Uri filepath = data.getData();
            final int flags = data.getFlags() & (FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            ContentResolver resolver = Objects.requireNonNull(getContext()).getContentResolver();
            if(Build.VERSION.SDK_INT < 19) {
                getContext().getApplicationContext().grantUriPermission("org.flval.discordwebview", filepath, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                assert filepath != null;
                resolver.takePersistableUriPermission(filepath, flags);
            }
            PreferenceCategory thiscategory = findPreference("cssfiles");
            CheckBoxPreference pref = new CheckBoxPreference(getContext());
            assert filepath != null;
            pref.setTitle(filepath.toString());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().equals("true")) {
                    pref.setChecked(true);
                } else {
                    pref.setChecked(false);
                }
                editor.putString("PATH" + preference, newValue.toString()).commit();
                editor.putBoolean("requireReload", true).commit();
                return true;
            });
            editor.putString("PATH" + filepath, "true").commit();
            editor.putBoolean("requireReload", true).commit();
            assert thiscategory != null;
            thiscategory.addPreference(pref);
            pref.setChecked(true);
        }
    }
    List<String> paths = new ArrayList<>();
    private void setmode() {
        SwitchPreference enabled = findPreference("enabled");
        Preference addfile = findPreference("addfile");
        cat = findPreference("cssfiles");
        assert cat != null;
        int listCount = cat.getPreferenceCount();
        if (!deletemode) {
            for (int i = 1; i<listCount; i++) {
                CheckBoxPreference currentPref = (CheckBoxPreference) cat.getPreference(i);
                paths.add(String.valueOf(currentPref.isChecked()));
                currentPref.setChecked(false);
            }
            assert enabled != null;
            enabled.setVisible(false);
            assert addfile != null;
            addfile.setVisible(false);
            deletemode = true;
            deleteButton.setIcon(R.drawable.ic_baseline_check_24);
        } else {
            for (int i = 1; i<listCount; i++) {
                CheckBoxPreference currentPref = (CheckBoxPreference) cat.getPreference(i);
                if (currentPref.isChecked()) {
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
            editor.putBoolean("requireReload", true).commit();
            assert enabled != null;
            enabled.setVisible(true);
            assert addfile != null;
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
                break;
            case android.R.id.home:
                requireActivity().getSupportFragmentManager().popBackStack();
                break;
        }
        return true;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.csspref, rootKey);
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        editor = sharedPreferences.edit();
        SwitchPreference enabled = findPreference("enabled");
        Map<String, ?> saveddata = sharedPreferences.getAll();
        PreferenceCategory thiscat = findPreference("cssfiles");
        for(Map.Entry<String, ?> entry : saveddata.entrySet()) {
            if (entry.toString().startsWith("PATH")) {
                CheckBoxPreference pref = new CheckBoxPreference(Objects.requireNonNull(getContext()));
                String realentry = entry.toString().substring(4);
                String[] entryParts = realentry.split("=");
                pref.setTitle(entryParts[0]);
                if (entryParts[1].equals("true")) {
                    pref.setChecked(true);
                } else {
                    pref.setChecked(false);
                }
                pref.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue.toString().equals("true")) {
                        pref.setChecked(true);
                    } else {
                        pref.setChecked(false);
                    }
                    editor.putBoolean("requireReload", true).commit();
                    editor.putString("PATH" + preference, newValue.toString()).apply();
                    return true;
                });
                assert thiscat != null;
                thiscat.addPreference(pref);
            }
        }
        assert enabled != null;
        enabled.setOnPreferenceChangeListener((preference, newValue) -> {
            PreferenceCategory cssfiles = findPreference("cssfiles");
            Log.d("PREFERENCE CHANGED", "IDK MAN");
            Log.d("NEW VALUE OF PREFERENCE " + preference, Boolean.toString((Boolean) newValue));
            editor.putBoolean("CSSEnabled", (Boolean) newValue).commit();
            assert cssfiles != null;
            cssfiles.setEnabled((Boolean) newValue);
            editor.putBoolean("requireReload", true).commit();
            return true;
        });
        Preference addfile = findPreference("addfile");
        assert addfile != null;
        addfile.setOnPreferenceClickListener(preference -> {
            Intent file = new Intent(ACTION_OPEN_DOCUMENT);
            file.setType("text/css");
            file.addCategory(Intent.CATEGORY_OPENABLE);
            file.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(file, 8778);
            return true;
        });
    }
}