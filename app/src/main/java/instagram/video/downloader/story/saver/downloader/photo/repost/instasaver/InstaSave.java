package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

public class InstaSave extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

    }
}