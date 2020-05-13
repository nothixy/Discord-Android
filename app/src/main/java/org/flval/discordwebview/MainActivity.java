package org.flval.discordwebview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    WebView discord;
    private Menu OptionMenu;
    private Context context;
    boolean loaded = false;
    boolean hascustomtheme = false;

    public boolean onCreateOptionsMenu(Menu menu) {
        OptionMenu = menu;
        getMenuInflater().inflate(R.menu.bar_webview, menu);
        OptionMenu.findItem(R.id.app_bar_people).setVisible(false);
        return true;
    }

    String currenturl;
    final Boolean[] menuopen = {false};
    final Boolean[] peopleopen = {false};

    public void onBackPressed() {
        menuleftswitch();
    }

    public void menuleftswitch() {
        if (!menuopen[0]) {
            injectCSS("style_people_close.css");
            peopleopen[0] = false;
            injectCSS("style_menuleft_open.css");
            menuopen[0] = true;
        } else {
            injectCSS("style_menuleft_close.css");
            menuopen[0] = false;
        }
    }

    public void peopleswitch() {
        if (!peopleopen[0]) {
            injectCSS("style_menuleft_close.css");
            menuopen[0] = false;
            injectCSS("style_people_open.css");
            peopleopen[0] = true;
        } else {
            injectCSS("style_people_close.css");
            peopleopen[0] = false;
        }
    }

    public boolean onSupportNavigateUp() {
        peopleswitch();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("requireReload", false)) {
            loaded = false;
            discord.reload();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("requireReload", false).commit();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menuleftswitch();
                break;
            case R.id.app_bar_people:
                peopleswitch();
                break;
            case R.id.reload:
                loaded = false;
                discord.reload();
                break;
            case R.id.Settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar6));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24px);
        this.context = getApplicationContext();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean isCall = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isCall", false);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (isCall) {
            audioManager.setSpeakerphoneOn(false);
        } else {
            audioManager.setSpeakerphoneOn(true);
        }

        List<String> perms = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!perms.isEmpty()) {
            int MY_PERMISSIONS_ALL = 1;
            int size = perms.size();
            ActivityCompat.requestPermissions(MainActivity.this, perms.toArray(new String[size]), MY_PERMISSIONS_ALL);
        }


        discord = findViewById(R.id.webview);
        // Here we want the desktop site
        String newUA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36";
        discord.getSettings().setUserAgentString(newUA);
        discord.getSettings().setMediaPlaybackRequiresUserGesture(false);
        CookieManager.getInstance().setAcceptCookie(true);
        discord.getSettings().setDomStorageEnabled(true);
        discord.getSettings().setAllowFileAccessFromFileURLs(true);
        discord.getSettings().setAllowFileAccess(true);
        discord.getSettings().setAllowUniversalAccessFromFileURLs(true);

        // Enable Javascript
        discord.getSettings().setJavaScriptEnabled(true);

        WebView.setWebContentsDebuggingEnabled(true);
        // Add a WebViewClient
        discord.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });

        discord.setWebChromeClient(new WebChromeClient() {
            String channelprefix = "@CHANNEL :";

            public boolean onConsoleMessage(ConsoleMessage response) {
                if (response.message().startsWith(channelprefix)) {
                    String chantext = response.message().substring(channelprefix.length());
                    Objects.requireNonNull(getSupportActionBar()).setTitle(chantext);
                }
                return true;
            }

            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    ConstraintLayout loading = findViewById(R.id.loading);
                    loading.setVisibility(View.INVISIBLE);
                    currenturl = discord.getUrl();
                    if (currenturl.contains("/channels/") && !loaded) {
                        injectCSS("style.css");
                        injectCSS("style_people_close.css");
                        injectCSS("style_menuleft_close.css");
                        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                        boolean hidePaid = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("hidePaid", false);
                        Log.d("DO WE NEED TO LOAD HIDEPAID ?", String.valueOf(hidePaid));
                        if (hidePaid) {
                            injectCSS("hidepaid.css");
                        }
                        loaded = true;
                    }
                    Map<String, ?> saveddata = PreferenceManager.getDefaultSharedPreferences(context).getAll();
                    boolean cssenabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("CSSEnabled", false);
                    Log.d("IS CSS ENABLED ?", Boolean.toString(cssenabled));
                    if (cssenabled) {
                        for (Map.Entry<String, ?> entry : saveddata.entrySet()) {
                            if (entry.toString().startsWith("PATH")) {
                                String realentry = entry.toString().substring(4);
                                String[] entryParts = realentry.split("=");
                                if (entryParts[1].equals("true")) {
                                    Log.d("LOADING CSS FILE", entryParts[0]);
                                    injectCSSfromstorage(entryParts[0]);
                                    hascustomtheme = true;
                                }
                            }
                        }
                    }
                    if (!hascustomtheme) {
                        changecolor();
                    }
                    if (currenturl.contains("/channels/") && (!currenturl.contains("@me") || currenturl.endsWith("@me"))) {
                        OptionMenu.findItem(R.id.app_bar_people).setVisible(true);
                    } else {
                        OptionMenu.findItem(R.id.app_bar_people).setVisible(false);
                    }
                    discord.loadUrl("javascript:(function() { console.log('" + channelprefix + "', document.getElementsByClassName('title-29uC1r').item(0).textContent) })()");
                    injectCSS("style_menuleft_close.css");
                    menuopen[0] = false;
                }
            }
        });
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent;
        appLinkIntent = getIntent();
        if (Objects.equals(appLinkIntent.getAction(), "android.intent.action.VIEW")) {
            String incomingURL = appLinkIntent.getDataString();
            discord.loadUrl(incomingURL);
        } else {
            discord.loadUrl("https://discord.com/login");
        }
    }

    // Inject CSS method: read style.css from assets folder
// Append stylesheet to document head
    private void injectCSS(String cssfile) {
        WebView view = findViewById(R.id.webview);
        try {
            InputStream css = getAssets().open(cssfile);
            byte[] cssbuffer = new byte[css.available()];
            css.read(cssbuffer);
            css.close();
            String cssencoded = Base64.encodeToString(cssbuffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + cssencoded + "');" +
                    "parent.appendChild(style);" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectCSSfromstorage(String cssfile) {
        WebView view = findViewById(R.id.webview);
        try {
            Uri uri = Uri.parse(cssfile);
            InputStream css = getContentResolver().openInputStream(uri);
            assert css != null;
            byte[] cssbuffer = new byte[css.available()];
            css.read(cssbuffer);
            css.close();
            String cssencoded = Base64.encodeToString(cssbuffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + cssencoded + "');" +
                    "parent.appendChild(style);" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void changecolor() {
        String darkmode = PreferenceManager.getDefaultSharedPreferences(context).getString("mode", "sysui");
        assert darkmode != null;
        Log.d("DARK MODE VALUE", darkmode);
        switch (darkmode) {
            case "dark":
                getDarkMode("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                Log.d("injecting CSS file", "colors-light.css");
                injectCSS("colors-light.css");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "sysui":
                getDarkMode("sysui");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    private void getDarkMode(String origin) {
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == 32 || origin.equals("dark")) {
            boolean blackdarkmode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("darkblack", false);
            if (blackdarkmode) {
                injectCSS("colors-darker.css");
            } else {
                injectCSS("colors-dark.css");
            }
        } else {
            injectCSS("colors-light.css");
        }
    }
}