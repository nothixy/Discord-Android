package org.flval.discordwebview;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingslayout, new SettingsFragment())
                .addToBackStack(null)
                .commit();


    }
}