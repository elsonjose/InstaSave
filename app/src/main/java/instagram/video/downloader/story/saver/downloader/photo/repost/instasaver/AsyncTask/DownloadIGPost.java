package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.AsyncTask;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.room.Room;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import java.lang.ref.WeakReference;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.InstaLink;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.LinkDatabase;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.ResponseModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.HttpHandler;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Notification;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Util;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker.InstaDownloadWorker;

public class DownloadIGPost extends AsyncTask<String, String, Integer> {

    WeakReference<Context> context;
    private static final String TAG = "DownloadIGPost";

    public static final int LOGIN_NEEDED=-1;
    public static final int NO_MEDIA_FOUND=-2;
    public static final int STORY_EXPIRED=-4;
    public static final int UNIDENTIFIED_URL=-5;
    public static final int UNKNOWN_ERROR=-6;


    public DownloadIGPost(Context context) {
        this.context = new WeakReference<Context>(context);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Toast t = Toast.makeText(context.get(), values[0], Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        LinkDatabase linkDatabase = Room.databaseBuilder(context.get(), LinkDatabase.class, Constant.LINK_DB).fallbackToDestructiveMigration().build();
        Log.i(TAG, "url: "+strings[0]);

        String type = getUrlType(strings[0]);
        if(!TextUtils.isEmpty(type))
        {
            HttpHandler sh = new HttpHandler();
            String baseUrl= "https://swift-status-saver-backend.herokuapp.com/instagram/";
            String cookie = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COOKIE, "");
            if(!TextUtils.isEmpty(cookie))
            {
                // has cookie
                String jsonData = sh.makeServiceCall(baseUrl,strings[0],cookie);
//                    Log.i(TAG, "doInBackground: "+jsonData);
                ResponseModel responseModel = new ResponseModel();
                if (type.equals("posts"))
                {
                    responseModel = sh.getPosts(jsonData);
                }
                else if (type.equals("reel"))
                {
                    responseModel = sh.getReels(jsonData);
                }
                else
                {
                    responseModel = sh.getStories(jsonData);
                }

                if(responseModel.getStatus() ==Constant.FETCH_SUCCESSFUL)
                {
                    if(responseModel.getModelList().size()>0)
                    {
                        return startWorker(linkDatabase,responseModel);
                    }
                    else
                    {
                        publishProgress("No media found");
                        return NO_MEDIA_FOUND;
                    }
                }
                else
                {
                    int status = responseModel.getStatus();
                    if (status == Constant.COOKIE_EXPIRED)
                    {
                        publishProgress("Need to login. Check notification");
                        return LOGIN_NEEDED;
                    }
                    else if (status == Constant.STORY_EXPIRED)
                    {
                        publishProgress("Story expired");
                        return STORY_EXPIRED;
                    }
                    else
                    {
                        publishProgress("Unknown error");
                        return UNKNOWN_ERROR;
                    }
                }
            }
            else
            {
                publishProgress("Need to login. Check notification");
                return  LOGIN_NEEDED;
            }
        }
        else
        {
            publishProgress("Invalid link");
            return UNIDENTIFIED_URL;
        }

    }

    private int startWorker(LinkDatabase linkDatabase, ResponseModel responseModel) {

        int count=0;

        for(int i=0;i<responseModel.getModelList().size();i++)
        {
            Log.i(TAG, "startWorker: loop "+i);

            if(!linkDatabase.getLinkDao().checkInstaLinkExists(responseModel.getModelList().get(i).getUrl()))
            {
                Log.i(TAG, "startWorker: adding to db");
                count+=1;
                linkDatabase.getLinkDao().addLink(new InstaLink(responseModel.getModelList().get(i).getUrl(), 0,false,true,false,responseModel.getModelList().get(i).getType()));
            }
            else
            {
                Log.i(TAG, "startWorker: already in db");
            }
        }
        if(new Util(context.get()).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.ENQUEUED && new Util(context.get()).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.RUNNING)
        {
            OneTimeWorkRequest downloadWork  = new OneTimeWorkRequest.Builder(InstaDownloadWorker.class).build();
            WorkManager.getInstance(context.get()).beginUniqueWork(Constant.INSTA_DOWNLOAD_WORKER_TAG, ExistingWorkPolicy.KEEP,downloadWork).enqueue();
        }
        return count;

    }

    private String getUrlType(String string) {
        if(string.contains("/p/"))
        {
            return "posts";
        }
        else if(string.contains("/reel/"))
        {
            return "reel";
        }
        else if(string.contains("/stories/"))
        {
            return "stories";
        }
        return "";
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if(integer==LOGIN_NEEDED)
        {
            new Notification(context.get()).showInstagramLogin(Constant.instaLoginNotificationID, "Private Content", "Tap here to login to Insta and try it.");
        }
        if(integer>0)
        {
            Toast t = Toast.makeText(context.get(), "Downloading", Toast.LENGTH_SHORT);
            t.show();
            try {
                ClipboardManager clipboard = (ClipboardManager) context.get().getSystemService(Context.CLIPBOARD_SERVICE);
                if(clipboard.hasPrimaryClip())
                {
                    String textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim();
                    if (clipboard.getPrimaryClip().getItemCount() > 0 && !TextUtils.isEmpty(textToPaste) && textToPaste.contains("instagram.com/"))
                    {
                        ClipData clipData = ClipData.newPlainText("", "");
                        clipboard.setPrimaryClip(clipData);
                    }
                }

            }
            catch (Exception e)
            {
                Log.i(TAG, "onPostExecute: "+e);
            }
        }
    }
}