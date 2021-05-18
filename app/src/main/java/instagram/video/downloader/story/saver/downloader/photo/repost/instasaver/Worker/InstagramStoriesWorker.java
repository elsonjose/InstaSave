package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.HttpHandler;

public class InstagramStoriesWorker extends Worker {

    private static final String TAG = "InstagramStoriesWorker";
    WeakReference<Context> context;

    public InstagramStoriesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: InstagramStoriesWorker");
        HttpHandler sh = new HttpHandler();
        String baseUrl = "https://swift-status-saver-backend.herokuapp.com/story/";
        String cookie = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COOKIE, "");
        if (!TextUtils.isEmpty(cookie)) {
            String jsonData = sh.makeServiceCall(baseUrl, "", cookie);
            try {
                SharedPreferences.Editor editor = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).edit();
                editor.putLong(Constant.STORY_LAST_CHECKED_AT,System.currentTimeMillis()/1000);
                editor.apply();
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                {
                    String fileName = "story.txt";
                    File outputFile = new File(context.get().getFilesDir()+fileName);
                    if (!outputFile.exists())
                        outputFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(jsonData.getBytes());
                    fos.close();

                }
                else
                {
                    OutputStreamWriter writer = new OutputStreamWriter(context.get().openFileOutput("story.txt", Context.MODE_PRIVATE));
                    writer.write(jsonData);
                    writer.close();
                }

                Log.i(TAG, "doWork: file written");
                Intent broadcastIntent=new Intent();
                broadcastIntent.setAction("com.refresh.screen");
                broadcastIntent.putExtra(Constant.BROADCAST_ACTION,Constant.INSTA_STORY_UPDATED);
                context.get().sendBroadcast(broadcastIntent);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "doWork: "+e);
            }
        }
        return Result.success();
    }
}
