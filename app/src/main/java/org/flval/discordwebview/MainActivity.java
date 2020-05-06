package org.flval.discordwebview;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    PreferenceManager preferenceManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private ValueCallback<Uri[]> UploadMessage;
    WebView discord;
    private Menu OptionMenu;
    private Context context;

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
    public boolean menuleftswitch() {
        if (!menuopen[0]) {
            injectCSS("style_people_close.css");
            peopleopen[0] = false;
            injectCSS("style_menuleft_open.css");
            menuopen[0] = true;
        } else {
            injectCSS("style_menuleft_close.css");
            menuopen[0] = false;
        };
        return true;
    }
    public boolean peopleswitch() {
        if (!peopleopen[0]) {
            Log.d("Opening people menu", "done");
            injectCSS("style_menuleft_close.css");
            menuopen[0] = false;
            injectCSS("style_people_open.css");
            peopleopen[0] = true;
        } else {
            injectCSS("style_people_close.css");
            peopleopen[0] = false;
        };
        return true;
    }
    public boolean onSupportNavigateUp() {
        peopleswitch();
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menuleftswitch();
                return true;
            case R.id.app_bar_people:
                peopleswitch();
                return true;
            case R.id.Settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                Log.d("test", "test");
                return true;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar6));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24px);


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
            ActivityCompat.requestPermissions(MainActivity.this, perms.toArray(new String[perms.size()]), MY_PERMISSIONS_ALL);
        }
        this.context = getApplicationContext();


        discord = (WebView) findViewById(R.id.webview);
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

        discord.setWebContentsDebuggingEnabled(true);
        // Add a WebViewClient
        discord.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });

        discord.setWebChromeClient(new WebChromeClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
            String channelprefix = "@CHANNEL :";

            public boolean onConsoleMessage(ConsoleMessage response) {
                if (response.message().startsWith(channelprefix)) {
                    String chantext = response.message().substring(channelprefix.length());
                    Log.d("Channel", chantext);
                    getSupportActionBar().setTitle(chantext);
                } else if (response.message() == "SETTINGSON") {
                    replace("on");
                } else if (response.message() == "SETTINGSOFF") {
                    replace("off");
                }
                return true;
            }

            private void replace(String sw) {
                if (sw == "on") {
                    Log.d("SETTINGS ON BINGO", "WIN");
                } else if (sw == "off") {
                    Log.d("SETTINGS OFF BINGO", "WIN");
                }
            }

            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    Boolean loaded = false;
                    ConstraintLayout loading = (ConstraintLayout) findViewById(R.id.loading);
                    loading.setVisibility(View.INVISIBLE);
                    currenturl = discord.getUrl();
                    if (currenturl.endsWith("@me") && loaded == false) {
                        injectCSS("style.css");
                        injectCSS("style_people_close.css");
                        injectCSS("style_menuleft_close.css");
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        loaded = true;
                    }
                    Map<String, ?> saveddata = PreferenceManager.getDefaultSharedPreferences(context).getAll();
//                  sharedPreferences = preferenceManager.getSharedPreferences();
                    Boolean cssenabled = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean("CSSEnabled", false);
                    if (cssenabled) {
                        for (Map.Entry<String, ?> entry : saveddata.entrySet()) {
                            if (entry.toString().startsWith("PATH")) {
                                String realentry = entry.toString().substring(4);
                                String[] entryParts = realentry.split("=");
                                if (entryParts[1].equals("true")) {
                                    injectCSSfromstorage(entryParts[0]);
                                }
                            }
                        }
                    }
                    if (currenturl.contains("/channels/") && !currenturl.contains("@me")) {
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
        Intent appLinkIntent = getIntent();
        if (appLinkIntent.getAction().equals("android.intent.action.VIEW")) {
            String appLinkAction = appLinkIntent.getAction();
            Uri appLinkData = appLinkIntent.getData();
            String incomingURL = appLinkIntent.getDataString();
            Log.d("INCOMING URL", incomingURL);
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
            InputStream css = new FileInputStream(cssfile);
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
}