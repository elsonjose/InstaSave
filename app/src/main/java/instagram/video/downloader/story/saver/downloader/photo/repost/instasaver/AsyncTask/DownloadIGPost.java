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
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.CookieUtils;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;
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
    public static final int UNIDENTIFIED_MEDIA_TYPE=-8;
    public static final int UNKNOWN_ERROR=-6;
    public static final int MISSING_KEYS=-7;


    public static final int STATUS_OK=200;


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

        String reqUrl = strings[0];

        if (reqUrl.startsWith("https://instagram.com/")) {
            reqUrl = reqUrl.replace("https://instagram.com/", "https://www.instagram.com/");
        }
        String cookie = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COOKIE, "");
        Log.i(TAG, "reqUrl: "+reqUrl);
        String type = getUrlType(strings[0]);
        if(!TextUtils.isEmpty(type))
        {
            ResponseModel model=null;

            if(type.equals(Constant.INSTAGRAM_POST))
            {
                // url is post
                String postBaseUrl = "https://www.instagram.com/p/";
                if(!reqUrl.contains("/?"))
                {
                    String[] urlIdentifier = reqUrl.split("/p/");
                    String urlIdentity = urlIdentifier[1];
                    if (urlIdentity.contains("/"))
                    {
                        int slashIndex = urlIdentity.indexOf("/");
                        urlIdentity = urlIdentity.substring(0,slashIndex);
                    }
                    reqUrl = postBaseUrl+urlIdentity+"/?";
                }

                String postIdentifier = reqUrl.substring(reqUrl.indexOf("p/") + 2, reqUrl.indexOf("/?"));
                if(postIdentifier.length() == 11)
                {
                    // url public
                    String apiReqUrl = reqUrl.substring(0,39);
                    apiReqUrl+="/?__a=1";
//                    Log.i(TAG, "doInBackground: apiReqUrl: "+apiReqUrl);
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonData = httpHandler.makeServiceCall(apiReqUrl,CookieUtils.getCookie("https://www.instagram.com/"));
//                    Log.i(TAG, "doInBackground: "+jsonData);
                    model = httpHandler.extractUrl(jsonData);
                }
                else if(postIdentifier.length() == 39)
                {
                    // url private
                    String apiReqUrl = reqUrl.substring(0,67);
                    apiReqUrl+="/?__a=1";
                    Log.i(TAG, "doInBackground: apiReqUrl: "+apiReqUrl);
                    HttpHandler httpHandler = new HttpHandler();
                    if(!TextUtils.isEmpty(cookie))
                    {
                        String jsonData = httpHandler.makeServiceCall(apiReqUrl,cookie);
//                        Log.i(TAG, "doInBackground: "+jsonData);
                        model = httpHandler.extractUrl(jsonData);
                    }
                    else
                    {
                        publishProgress("Login required for private post");
//                        Toast.makeText(context.get(), "Login required for private post", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            else if(type.equals(Constant.INSTAGRAM_REEL))
            {
                // url is reel
                String postBaseUrl = "https://www.instagram.com/reel/";
                if(!reqUrl.contains("/?"))
                {
                    String[] urlIdentifier = reqUrl.split("/reel/");
                    String urlIdentity = urlIdentifier[1];
                    if (urlIdentity.contains("/"))
                    {
                        int slashIndex = urlIdentity.indexOf("/");
                        urlIdentity = urlIdentity.substring(0,slashIndex);
                    }
                    reqUrl = postBaseUrl+urlIdentity+"/?";
                }
                String postIdentifier = reqUrl.substring(reqUrl.indexOf("reel/") + 5, reqUrl.indexOf("/?"));
                if(postIdentifier.length() == 11)
                {
                    // reel public
                    String apiReqUrl = reqUrl.substring(0,42);
                    apiReqUrl+="/?__a=1";
//                    Log.i(TAG, "doInBackground: apiReqUrl: "+apiReqUrl);
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonData = httpHandler.makeServiceCall(apiReqUrl,CookieUtils.getCookie("https://www.instagram.com/"));
//                    Log.i(TAG, "doInBackground: "+jsonData);
                    model = httpHandler.extractUrl(jsonData);
                }
                else if(postIdentifier.length() == 39)
                {
                    // reel private
                    String apiReqUrl = reqUrl.substring(0,70);
                    apiReqUrl+="/?__a=1";
//                    Log.i(TAG, "doInBackground: apiReqUrl: "+apiReqUrl);
                    HttpHandler httpHandler = new HttpHandler();
                    if(!TextUtils.isEmpty(cookie))
                    {
                        String jsonData = httpHandler.makeServiceCall(apiReqUrl,cookie);
//                        Log.i(TAG, "doInBackground: "+jsonData);
                        model = httpHandler.extractUrl(jsonData);
                    }
                    else
                    {
                        publishProgress("Login required for private reel");
//                        Toast.makeText(context.get(), "Login required for private reel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if(type.equals(Constant.INSTAGRAM_STORY))
            {
                // url is story
                if(!TextUtils.isEmpty(cookie))
                {

                    String postBaseUrl = "https://www.instagram.com/";

                    String[] urlIdentifier = reqUrl.split("/stories/");
                    String urlIdentity = urlIdentifier[1];
                    if (urlIdentity.contains("/"))
                    {
                        int slashIndex = urlIdentity.indexOf("/");
                        urlIdentity = urlIdentity.substring(0,slashIndex);
                    }
                    reqUrl = postBaseUrl+urlIdentity+"/?__a=1"; // to get userId
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonData = httpHandler.makeServiceCallForStory(reqUrl,cookie);
                    model = httpHandler.extractStoryUrl(jsonData);

                }
                else
                {
                    publishProgress("Login required for story");
                }

            }
            if(model!=null)
            {
                if(model.getStatus()==STATUS_OK)
                {
                    if(model.getModelList().size()>0)
                    {
                        startWorker(linkDatabase,model);
                        return STATUS_OK;
                    }
                    else
                    {
                        return NO_MEDIA_FOUND;
                    }
                }
                else
                {
                    return  model.getStatus();
                }
            }
            else
            {
                String errorLoc = "DownloadIGPost";
                String error = "response model is null for reqUrl "+reqUrl +" org_link "+strings[0];
                FirebaseLogger.logErrorData(errorLoc,error);
            }

        }
        else
        {
            publishProgress("Invalid link");
            return UNIDENTIFIED_URL;
        }

        return UNKNOWN_ERROR;
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
            return Constant.INSTAGRAM_POST;
        }
        else if(string.contains("/reel/"))
        {
            return Constant.INSTAGRAM_REEL;
        }
        else if(string.contains("/stories/"))
        {
            return Constant.INSTAGRAM_STORY;
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