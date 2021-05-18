package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.MainActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.CookieUtils;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker.InstagramStoriesWorker;

public class InstagramLoginActivity extends AppCompatActivity {

    private static final String TAG = "InstagramLoginActivity";


    Toolbar toolbar;
    LinearLayout instagramWrapper;
    WebView loginWebView;
    private String webViewUrl;
    private boolean ready = false;
    private final WebChromeClient webChromeClient = new WebChromeClient();
    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            webViewUrl = url;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            webViewUrl = url;
            final String mainCookie = CookieUtils.getCookie(url);
            if (TextUtils.isEmpty(mainCookie) || !mainCookie.contains("; ds_user_id=")) {
                ready = true;
                return;
            }
            if (mainCookie.contains("; ds_user_id=") && ready) {
                returnCookieResult(mainCookie);
            }
        }
    };

    private void returnCookieResult(final String mainCookie) {

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
                .Builder(InstagramStoriesWorker.class, 15, TimeUnit.MINUTES)
                .addTag(Constant.INSTA_STORY_WORKER_TAG)
                .build();
        WorkManager.getInstance(InstagramLoginActivity.this).enqueueUniquePeriodicWork(Constant.INSTA_STORY_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);

        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appDataPref.edit();
        editor.putString(Constant.COOKIE,mainCookie);
        editor.commit();
        Log.i(TAG, "returnCookieResult: cookie "+mainCookie);
        final Intent intent = new Intent(InstagramLoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        String theme = appDataPref.getString(Constant.THEME_KEY,Constant.THEME_VAL_SYSTEM);
        switch (theme)
        {
            case Constant.THEME_VAL_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                // disable manual selection
                break;
            case Constant.THEME_VAL_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                // check switch
                break;
            case Constant.THEME_VAL_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // uncheck switch
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_login);


        MainActivity mainActivity = MainActivity.getMainActivity();
        if(mainActivity!=null)
        {
            mainActivity.finish();
        }

        toolbar = findViewById(R.id.instagram_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        instagramWrapper = findViewById(R.id.instagram_wrapper);
        loginWebView = findViewById(R.id.webView);
        Button cookieBtn = findViewById(R.id.instagram_clear_cookies_btn);
        if(appDataPref.contains(Constant.COOKIE))
        {
            cookieBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            cookieBtn.setVisibility(View.GONE);
        }

        Log.i(TAG, "onCreate: COOKIE "+appDataPref.getString(Constant.COOKIE,null));

    }

    private void initWebView() {

        loginWebView.setWebChromeClient(webChromeClient);
        loginWebView.setWebViewClient(webViewClient);
        WebSettings webSettings = loginWebView.getSettings();
        if (webSettings != null) {
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36");
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getApplicationContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        loginWebView.loadUrl("https://instagram.com/");
    }

    @Override
    protected void onPause() {
        loginWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        loginWebView.removeAllViews();
        loginWebView.destroy();
        super.onDestroy();
    }

    public void logIntoInstagram(View view) {

        initWebView();
        hideInstagramWrapper();
        showInstagramWebView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()== android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showInstagramWebView() {

        loginWebView.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_in));
        loginWebView.getAnimation().start();
        loginWebView.setVisibility(View.VISIBLE);
    }

    private void showInstagramWrapper() {

        instagramWrapper.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_in));
        instagramWrapper.getAnimation().start();
        instagramWrapper.setVisibility(View.VISIBLE);
    }

    private void hideInstagramWebView() {

        loginWebView.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_out));
        loginWebView.getAnimation().start();
        loginWebView.setVisibility(View.GONE);
    }

    private void hideInstagramWrapper() {
        instagramWrapper.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_out));
        instagramWrapper.getAnimation().start();
        instagramWrapper.setVisibility(View.GONE);
    }

    public void clearCookies(View view) {

        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        if(appDataPref.contains(Constant.COOKIE))
        {
            SharedPreferences.Editor editor = appDataPref.edit();
            editor.remove(Constant.COOKIE);
            editor.apply();
            Toast t = Toast.makeText(this, "Cookie cleared", Toast.LENGTH_SHORT);
            t.show();
            Button cookieBtn = findViewById(R.id.instagram_clear_cookies_btn);
            if(appDataPref.contains(Constant.COOKIE))
            {
                cookieBtn.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_in));
                cookieBtn.getAnimation().start();
                cookieBtn.setVisibility(View.VISIBLE);
            }
            else
            {
                cookieBtn.setAnimation(AnimationUtils.loadAnimation(InstagramLoginActivity.this,android.R.anim.fade_out));
                cookieBtn.getAnimation().start();
                cookieBtn.setVisibility(View.GONE);
            }
        }
        else
        {
            Toast t = Toast.makeText(this, "Cookie empty", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(!instagramWrapper.isShown() && loginWebView.isShown())
        {
            hideInstagramWebView();
            showInstagramWrapper();

        }
        else
        {
            startActivity(new Intent(InstagramLoginActivity.this,MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}