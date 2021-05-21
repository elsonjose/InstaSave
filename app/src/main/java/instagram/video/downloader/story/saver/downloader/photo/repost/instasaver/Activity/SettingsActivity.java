package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.TimeUnit;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.BottomSheet.WebBottomSheetView;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.MainActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivityLogs";
    SwitchMaterial themeLightSwitch,themeDarkSwitch;
    Toolbar settingsToolbar;
    LinearLayout settingsThemeWrapper;
    TextView sysThemeInfoTextView;

    public void reviewApp(View v)
    {
        Log.i(TAG, "reviewApp: ask review clicked");
        askReview();
    }

    public void sendMail(View v)
    {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"instasaveapp079@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "InstaSave Feedback");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            FirebaseLogger.logErrorData("SettingsActivity sendMail ",ex.toString());
            showSnackBarMessage(findViewById(R.id.settings_root_layout),"No email clients installed on device");
        }
    }

    private void showSnackBarMessage(View view, String message) {
        Snackbar.make(view,message, BaseTransientBottomBar.LENGTH_SHORT).show();
    }


    public void viewTerms(View v)
    {
        WebBottomSheetView termsWebSheet = new WebBottomSheetView("https://instasave-3dd0a.web.app/terms.html",settingsToolbar.findViewById(R.id.settings_progressbar),v);
        termsWebSheet.show((FragmentManager) getSupportFragmentManager(),termsWebSheet.getTag());
        v.setEnabled(false);
        settingsToolbar.findViewById(R.id.settings_progressbar).setVisibility(View.VISIBLE);
    }

    public void viewVersion(View v)
    {
        showSnackBarMessage(findViewById(R.id.settings_root_layout),"Version 1.0.19");
    }

    public void viewPolicy(View v)
    {
        settingsToolbar.findViewById(R.id.settings_progressbar).setVisibility(View.VISIBLE);
        WebBottomSheetView privacyWebSheet = new WebBottomSheetView("https://instasave-3dd0a.web.app/",settingsToolbar.findViewById(R.id.settings_progressbar),v);
        privacyWebSheet.show((FragmentManager) getSupportFragmentManager(),privacyWebSheet.getTag());
        v.setEnabled(false);
    }


    public void viewSysThemePolicy(View v)
    {
        settingsToolbar.findViewById(R.id.settings_progressbar).setVisibility(View.VISIBLE);
        WebBottomSheetView privacyWebSheet = new WebBottomSheetView("https://instasave-3dd0a.web.app/theme.html",settingsToolbar.findViewById(R.id.settings_progressbar),v);
        privacyWebSheet.show((FragmentManager) getSupportFragmentManager(),privacyWebSheet.getTag());
        v.setEnabled(false);

    }

    public void viewLicense(View v)
    {
        settingsToolbar.findViewById(R.id.settings_progressbar).setVisibility(View.VISIBLE);
        WebBottomSheetView privacyWebSheet = new WebBottomSheetView("https://swift-27f65.web.app/license.html",settingsToolbar.findViewById(R.id.settings_progressbar),v);
        privacyWebSheet.show((FragmentManager) getSupportFragmentManager(),privacyWebSheet.getTag());
        v.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
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
        setContentView(R.layout.activity_settings);


        settingsToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        themeLightSwitch = findViewById(R.id.setting_light_theme_switch);
        themeDarkSwitch = findViewById(R.id.setting_dark_theme_switch);

        settingsThemeWrapper = findViewById(R.id.settings_theme_wrapper);
        sysThemeInfoTextView = findViewById(R.id.setting_theme_info_textview);



        if(theme.equals(Constant.THEME_VAL_LIGHT))
        {
            themeLightSwitch.setChecked(true);
            themeDarkSwitch.setChecked(false);
            themeLightSwitch.setText("Light theme enabled");
        }
        else if(theme.equals(Constant.THEME_VAL_DARK))
        {
            themeDarkSwitch.setText("Dark theme enabled");
            themeLightSwitch.setChecked(false);
            themeDarkSwitch.setChecked(true);
        }


        if(!themeDarkSwitch.isChecked() && !themeLightSwitch.isChecked())
        {
            sysThemeInfoTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            sysThemeInfoTextView.setVisibility(View.GONE);
        }




        themeLightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    themeDarkSwitch.setChecked(false);
                    themeLightSwitch.setText("Light theme enabled");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    appDataPrefEditor.putString(Constant.THEME_KEY,Constant.THEME_VAL_LIGHT);
                    appDataPrefEditor.apply();
                    TransitionManager.beginDelayedTransition(findViewById(R.id.setting_item_wrapper),new AutoTransition());
                    sysThemeInfoTextView.setVisibility(View.GONE);
                }
                else
                {
                    themeLightSwitch.setText("Enable light theme");
                    if(!themeDarkSwitch.isChecked())
                    {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        appDataPrefEditor.putString(Constant.THEME_KEY,Constant.THEME_VAL_SYSTEM);
                        appDataPrefEditor.apply();
                        TransitionManager.beginDelayedTransition(findViewById(R.id.setting_item_wrapper),new AutoTransition());
                        sysThemeInfoTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        themeDarkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    themeLightSwitch.setChecked(false);
                    themeDarkSwitch.setText("Dark theme enabled");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    appDataPrefEditor.putString(Constant.THEME_KEY,Constant.THEME_VAL_DARK);
                    appDataPrefEditor.apply();
                    TransitionManager.beginDelayedTransition(findViewById(R.id.setting_item_wrapper),new AutoTransition());
                    sysThemeInfoTextView.setVisibility(View.GONE);
                }
                else
                {
                    themeLightSwitch.setText("Enable dark theme");
                    if(!themeLightSwitch.isChecked())
                    {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        appDataPrefEditor.putString(Constant.THEME_KEY,Constant.THEME_VAL_SYSTEM);
                        appDataPrefEditor.apply();
                        TransitionManager.beginDelayedTransition(findViewById(R.id.setting_item_wrapper),new AutoTransition());
                        sysThemeInfoTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();
        if(menuItemId == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void askReview() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp.status.saver.downloader.share"));
        startActivity(browserIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        finish();
    }
}