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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;
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
//        Log.i(TAG, "doWork: InstagramStoriesWorker");
        HttpHandler sh = new HttpHandler();
        String baseUrl = "https://i.instagram.com/api/v1/feed/reels_tray/";
        String cookie = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COOKIE, "");
        if (!TextUtils.isEmpty(cookie)) {

            try {

                URL storyUrl = new URL(baseUrl);
                HttpsURLConnection con = (HttpsURLConnection) storyUrl.openConnection();
                con.setRequestMethod("GET");
                con.addRequestProperty("Accept","application/json");
                con.addRequestProperty("Accept-Language","en-US,en;q=0.5");
                con.addRequestProperty("User-Agent","Instagram 10.26.0 (iPhone7,2; iOS 10_1_1; en_US; en-US; scale=2.00; gamut=normal; 750x1334) AppleWebKit/420+");
                con.addRequestProperty("Connection","keep-alive");
                con.addRequestProperty("Cookie",cookie);
                con.setInstanceFollowRedirects(false);
                con.connect();
                if(con.getResponseCode()== HttpURLConnection.HTTP_OK)
                {
                    InputStream in = new BufferedInputStream(con.getInputStream());
                    String response = new HttpHandler().convertStreamToString(in);
                    JSONObject object = new JSONObject(response);
                    if (object.has("tray"))
                    {

                        JSONArray storyJsonArray = new JSONArray();

                        JSONArray storiesArray = object.getJSONArray("tray");
                        int storiesCount = storiesArray.length();
                        for(int i=0;i<storiesCount;i++)
                        {
                            JSONObject storyJson = new JSONObject();

                            JSONObject tray = storiesArray.getJSONObject(i);

                            JSONObject user = tray.getJSONObject("user");
                            String userName = user.getString("username");
                            String userProfileUrl = user.getString("profile_pic_url");
                            String userid = user.getString("pk");

                            JSONArray storiesList = new JSONArray();

                            if (tray.has("items"))
                            {
                                JSONArray items = tray.getJSONArray("items");
                                for(int j=0;j<items.length();j++)
                                {
                                    JSONObject story = new JSONObject();
                                    long eta = items.getJSONObject(j).getLong("taken_at")+(24*60*60);
                                    int type = items.getJSONObject(j).getInt("media_type");
                                    if(type==1)
                                    {

                                        String itemType = Constant.GRAPH_IMAGE;
                                        String url = items.getJSONObject(j).getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");
                                        story.put("expire_at",eta);
                                        story.put("type",itemType);
                                        story.put("url",url);
                                    }
                                    else if(type==2)
                                    {
                                        String itemType = Constant.GRAPH_VIDEO;
                                        String url = items.getJSONObject(j).getJSONArray("video_versions").getJSONObject(0).getString("url");
                                        story.put("expire_at",eta);
                                        story.put("type",itemType);
                                        story.put("url",url);
                                    }
                                    storiesList.put(story);

                                }
                            }
                            storyJson.put("name",userName);
                            storyJson.put("dp",userProfileUrl);
                            storyJson.put("uid",userid);
                            storyJson.put("stories",storiesList);

                            storyJsonArray.put(storyJson);
                        }

                        JSONObject userStoryObj = new JSONObject();
                        userStoryObj.put("stories",storyJsonArray);

//                        Log.i(TAG, "doWork: "+userStoryObj.toString());

                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                        {
                            String fileName = "story.txt";
                            File outputFile = new File(context.get().getFilesDir()+fileName);
                            if (!outputFile.exists())
                                outputFile.createNewFile();
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            fos.write(userStoryObj.toString().getBytes());
                            fos.close();

                        }
                        else
                        {
                            OutputStreamWriter writer = new OutputStreamWriter(context.get().openFileOutput("story.txt", Context.MODE_PRIVATE));
                            writer.write(userStoryObj.toString());
                            writer.close();
                        }

//                        Log.i(TAG, "doWork: file written");
                        Intent broadcastIntent=new Intent();
                        broadcastIntent.setAction("com.refresh.screen");
                        broadcastIntent.putExtra(Constant.BROADCAST_ACTION,Constant.INSTA_STORY_UPDATED);
                        context.get().sendBroadcast(broadcastIntent);
                    }
                }
            }
            catch (Exception e)
            {
//                Log.i(TAG, "doWork: "+e);
                FirebaseLogger.logErrorData("InstagramStoriesWorker ",e.toString());
            }

        }
        return Result.success();
    }
}
