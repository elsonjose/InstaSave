package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.AsyncTask.DownloadIGPost;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Notification;


public class DownloadInstaActivity extends Activity {

    private static final String TAG = "DownloadInstaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type.startsWith("text/plain")) {
                String instaUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (instaUrl != null && !TextUtils.isEmpty(instaUrl) && instaUrl.contains("instagram.com")) {
                    if (instaUrl.startsWith("https://instagram.com/")) {
                        instaUrl = instaUrl.replace("https://instagram.com/", "https://www.instagram.com/");
                    }

                    new DownloadIGPost(DownloadInstaActivity.this).execute(instaUrl);


                } else {
                    new Notification(DownloadInstaActivity.this).displayInstaDownload(Constant.instaDownloadNotificationID, "Download Failed", "No a valid instagram link");
                    finish();
                }
            } else {
                new Notification(DownloadInstaActivity.this).displayInstaDownload(Constant.instaDownloadNotificationID, "Download Failed", "Unsuitable format found");
                finish();
            }
        }
        finish();
    }

}