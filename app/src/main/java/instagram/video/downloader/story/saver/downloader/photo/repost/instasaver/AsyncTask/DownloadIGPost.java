package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.AsyncTask;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.InstaLink;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.LinkDatabase;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.InstaSave;
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
    String orgUrl="";

    public DownloadIGPost(Context context) {
        this.context = new WeakReference<Context>(context);
        orgUrl = "";
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Toast t = Toast.makeText(context.get(), values[0], Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        LinkDatabase linkDatabase = Room.databaseBuilder(context.get(), LinkDatabase.class, Constant.LINK_DB).fallbackToDestructiveMigration().build();
        String cookie = context.get().getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COOKIE, "");

        orgUrl = strings[0];

        String reqUrl = strings[0];
//        Log.i(TAG, "doInBackground: "+reqUrl);

        if(reqUrl.startsWith("https://i.instagram.com/api/v1/feed/user/"))
        {
            if(TextUtils.isEmpty(cookie))
            {
                publishProgress("Login required for story");
                return LOGIN_NEEDED;
            }
            try {
                URL storyUrl = new URL(reqUrl);
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
                    InputStream inputStream = new BufferedInputStream(con.getInputStream());
                    String response = new HttpHandler().convertStreamToString(inputStream);
                    ResponseModel model = new HttpHandler().extractStoryUrl(response);
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
                        publishProgress("This story is not visible to you");
                        FirebaseLogger.logErrorData(errorLoc,error);
                    }
                }
                else if(con.getResponseCode()==429)
                {
                    publishProgress("Too many requests");
                    return UNKNOWN_ERROR;
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "doInBackground: "+e);
                return UNKNOWN_ERROR;
            }
        }
        else
        {
            if (reqUrl.startsWith("https://instagram.com/")) {
                reqUrl = reqUrl.replace("https://instagram.com/", "https://www.instagram.com/");
            }
//        Log.i(TAG, "reqUrl: "+reqUrl);
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
                        Log.i(TAG, "doInBackground: reqUrl "+reqUrl);
                        HttpHandler httpHandler = new HttpHandler();
                        String jsonData = httpHandler.makeServiceCallForStory(reqUrl,cookie);
                        if(jsonData.equals(String.valueOf(429)))
                        {
                            publishProgress("Too many requests.");
                        }
                        else if(jsonData!=null)
                        {
                            model = httpHandler.extractStoryUrl(jsonData);
                        }

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
                    publishProgress("This content is not visible to you");
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
        }



        return UNKNOWN_ERROR;
    }

    private int startWorker(LinkDatabase linkDatabase, ResponseModel responseModel) {

        int count=0;

        for(int i=0;i<responseModel.getModelList().size();i++)
        {
//            Log.i(TAG, "startWorker: loop "+i);

            if(!linkDatabase.getLinkDao().checkInstaLinkExists(responseModel.getModelList().get(i).getUrl()))
            {
//                Log.i(TAG, "startWorker: adding to db");
                count+=1;
                linkDatabase.getLinkDao().addLink(new InstaLink(responseModel.getModelList().get(i).getUrl(), 0,false,true,false,responseModel.getModelList().get(i).getType()));
            }
            else
            {
//                Log.i(TAG, "startWorker: already in db");
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
            if(!InstaSave.isActivityVisible())
            {
                Toast t = Toast.makeText(context.get(), "Downloading", Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }
}