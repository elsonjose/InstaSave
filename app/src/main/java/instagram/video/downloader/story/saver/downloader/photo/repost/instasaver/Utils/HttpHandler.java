package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.AsyncTask.DownloadIGPost;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramDownloadModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramStoryModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.ResponseModel;

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    public String makeServiceCall(String reqUrl,String cookie)
    {   String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Accept","application/json");
            conn.addRequestProperty("Accept-Language","en-US,en;q=0.5");
            conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0");
            conn.addRequestProperty("Connection","keep-alive");
            conn.addRequestProperty("Cookie",cookie);
            conn.connect();
            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
            }
            else
            {
                Log.i(TAG, "makeServiceCall: connection failed");
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e);
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
        return response;
    }

    public String makeServiceCallForStory(String reqUrl,String cookie)
    {   String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Accept","application/json");
            conn.addRequestProperty("Accept-Language","en-US,en;q=0.5");
            conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0");
            conn.addRequestProperty("Connection","keep-alive");
            conn.addRequestProperty("Cookie",cookie);
            conn.connect();
            if(conn.getResponseCode()== HttpURLConnection.HTTP_OK)
            {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
                JSONObject object = new JSONObject(response);
                if (object.has("graphql") && object.getJSONObject("graphql").has("user") && object.getJSONObject("graphql").getJSONObject("user").has("id"))
                {
                    String userId = object.getJSONObject("graphql").getJSONObject("user").getString("id");
                    URL storyUrl = new URL("https://i.instagram.com/api/v1/feed/user/"+userId+"/reel_media/");
                    HttpsURLConnection con = (HttpsURLConnection) storyUrl.openConnection();
                    con.setRequestMethod("GET");
                    con.addRequestProperty("Accept","application/json");
                    con.addRequestProperty("Accept-Language","en-US,en;q=0.5");
                    con.addRequestProperty("User-Agent","Instagram 10.26.0 (iPhone7,2; iOS 10_1_1; en_US; en-US; scale=2.00; gamut=normal; 750x1334) AppleWebKit/420+");
                    con.addRequestProperty("Connection","keep-alive");
                    con.addRequestProperty("Cookie",cookie);
                    con.connect();
                    if(con.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        InputStream inputStream = new BufferedInputStream(con.getInputStream());
                        response = convertStreamToString(inputStream);
                    }
                    else
                    {
                        Log.i(TAG, "makeServiceCall: connection failed");

                    }

                }
            }
            else
            {
                Log.i(TAG, "makeServiceCallForStory: connection failed");
                return null;
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e);
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public ResponseModel extractStoryUrl(String jsonData)
    {
        String latest_reel_media="latest_reel_media";

        ResponseModel model = new ResponseModel();
        List<InstagramDownloadModel> urlList = new ArrayList<>();
        if(jsonData!=null)
        {
            try {
                JSONObject object = new JSONObject(jsonData);
                if (object.has(latest_reel_media))
                {
                    // reel exists
                    JSONArray dataItems = object.getJSONArray("items");
                    for(int i=0;i<dataItems.length();i++)
                    {
                        JSONObject item = dataItems.getJSONObject(i);
                        int type = item.getInt("media_type");
                        if(type==1)
                        {
                            String urlType = "image";
                            String url= item.getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");
                            urlList.add(new InstagramDownloadModel(urlType,url));
                        }
                        else if(type==2)
                        {
                            String urlType = "video";
                            String url= item.getJSONArray("video_versions").getJSONObject(0).getString("url");
                            urlList.add(new InstagramDownloadModel(urlType,url));
                        }
                    }

                    model.setStatus(DownloadIGPost.STATUS_OK);
                    model.setModelList(urlList);
                }
                else
                {
                    model.setStatus(DownloadIGPost.STORY_EXPIRED);
                }
            } catch (Exception e)
            {
                if (e instanceof MissingKeyException)
                {
                    model.setStatus(DownloadIGPost.MISSING_KEYS);
                }
                else
                {
                    model.setStatus(DownloadIGPost.NO_MEDIA_FOUND);
                }
                FirebaseLogger.logErrorData("HttpHandler extractUrl",e.toString());
                Log.i(TAG, "extractUrl: "+e);
            }
        }
        else
        {
            model.setStatus(DownloadIGPost.UNKNOWN_ERROR);
            Log.i(TAG, "extractUrl: json data null");
        }
        return model;
    }

    public ResponseModel extractUrl(String jsonData)
    {
        String graphql="graphql",shortcode_media="shortcode_media",__typename="__typename";

        ResponseModel model = new ResponseModel();
        List<InstagramDownloadModel> urlList = new ArrayList<>();
        if(jsonData!=null)
        {
            try {
                JSONObject object = new JSONObject(jsonData);
                if (object.has(graphql) && object.getJSONObject(graphql).has(shortcode_media))
                {
                    JSONObject shortcode_media_obj = object.getJSONObject(graphql).getJSONObject(shortcode_media);

                    if(shortcode_media_obj.has(__typename))
                    {
                        String type = shortcode_media_obj.getString(__typename);
                        if(type.equals(Constant.GRAPH_SIDE_CAR))
                        {
                            JSONArray dataItems = shortcode_media_obj.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");
                            for(int i=0;i<dataItems.length();i++)
                            {
                                JSONObject item = dataItems.getJSONObject(i).getJSONObject("node");
                                if(item.getString(__typename).equals(Constant.GRAPH_IMAGE))
                                {
                                    String urlType = Constant.GRAPH_IMAGE;
                                    String url= item.getString("display_url");
                                    urlList.add(new InstagramDownloadModel(urlType,url));
                                }
                                else if(item.getString(__typename).equals(Constant.GRAPH_VIDEO))
                                {
                                    String urlType = Constant.GRAPH_VIDEO;
                                    String url= item.getString("video_url");
                                    urlList.add(new InstagramDownloadModel(urlType,url));
                                }
                            }
                        }
                        else if(type.equals(Constant.GRAPH_VIDEO))
                        {
                            String url = shortcode_media_obj.getString("video_url");
                            Log.i(TAG, "extractUrl: video "+url);
                            urlList.add(new InstagramDownloadModel(Constant.GRAPH_VIDEO,url));
                        }
                        else if(type.equals(Constant.GRAPH_IMAGE))
                        {
                            String url = shortcode_media_obj.getString("display_url");
                            urlList.add(new InstagramDownloadModel(Constant.GRAPH_IMAGE,url));
                        }


                        if(urlList.size()>0)
                        {
                            model.setStatus(DownloadIGPost.STATUS_OK);
                            model.setModelList(urlList);
                        }
                        else
                        {
                            model.setStatus(DownloadIGPost.UNIDENTIFIED_MEDIA_TYPE);
                        }

                    }
                    else
                    {
                        throw new MissingKeyException(__typename+" key missing");
                    }
                }
                else
                {
                    throw new MissingKeyException(graphql+" and "+shortcode_media+" key missing");
                }
            } catch (Exception e)
            {
                if (e instanceof MissingKeyException)
                {
                    model.setStatus(DownloadIGPost.MISSING_KEYS);
                }
                else
                {
                    model.setStatus(DownloadIGPost.NO_MEDIA_FOUND);
                }
                FirebaseLogger.logErrorData("HttpHandler extractUrl",e.toString());
                Log.i(TAG, "extractUrl: "+e);
            }
        }
        else
        {
            model.setStatus(DownloadIGPost.UNKNOWN_ERROR);
            Log.i(TAG, "extractUrl: json data null");
        }
        return model;
    }


}
