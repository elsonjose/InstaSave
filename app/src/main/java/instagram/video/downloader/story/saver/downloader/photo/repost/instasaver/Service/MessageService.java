package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Notification;

public class MessageService extends FirebaseMessagingService {

    private static final String TAG = "MessageServiceLog";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        remoteMessage.getData();
        if(remoteMessage.getData().size() > 0)
        {
            if(remoteMessage.getData().get("data").equals("update"))
            {
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    if(!remoteMessage.getData().get("name").equals(version))
                    {
                        new Notification(this).displayVersionUpdate(Constant.versionUpdateNotificationID,"Update Available","A critical update is available for download.");
                        Log.i(TAG, "onMessageReceived: showing update notification");
                    }
                    else
                    {
                        Log.i(TAG, "onMessageReceived: latest version");
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Log.i(TAG, "onMessageReceived: exp "+e.getMessage());
                }
            }
            else if(remoteMessage.getData().get("data").equals("review"))
            {
                SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                boolean reviewAsked = appDataPref.getBoolean(Constant.REVIEW_NOTIFIED,false);
                if(!reviewAsked)
                {
                    new Notification(this).displayReviewNotification(Constant.reviewNotificationID,"Enjoying Swift?","Help us by giving a review by tapping here.");
                    appDataPref.edit().putBoolean(Constant.REVIEW_NOTIFIED,true).commit();
                }
            }
        }
        Log.i(TAG, "onMessageReceived: "+remoteMessage.getData());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: token "+s);
    }
}
