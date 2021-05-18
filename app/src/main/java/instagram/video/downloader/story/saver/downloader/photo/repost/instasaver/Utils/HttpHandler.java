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
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramDownloadModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramStoryModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.ResponseModel;

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    public String makeServiceCall(String reqUrl)
    {   String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("Accept","application/json");
            conn.addRequestProperty("Accept-Language","en-US,en;q=0.5");
            conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0");
            conn.addRequestProperty("Connection","keep-alive");
            conn.setDoOutput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
        return response;
    }

    public String makeServiceCall(String reqUrl, String instaUrl, String cookie) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Accept","application/json");
            conn.addRequestProperty("Accept-Language","en-US,en;q=0.5");
            conn.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0");
            conn.addRequestProperty("Connection","keep-alive");
            conn.setDoOutput(true);
            Uri.Builder builder = new Uri.Builder().appendQueryParameter("url", instaUrl);
            builder .appendQueryParameter("cookie", cookie);
            String query = builder.build().getEncodedQuery();
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
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

    public ResponseModel getStories(String jsonData)
    {
        ResponseModel model = new ResponseModel();
        List<InstagramDownloadModel> modelList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("stories"))
            {
                JSONArray stories = jsonObject.getJSONArray("stories");
                for(int i=0;i<stories.length();i++)
                {
                    String type = stories.getJSONObject(i).getString("type");
                    String url = stories.getJSONObject(i).getString("url");
                    Log.i(TAG, "getStories: url"+url);
                    modelList.add(new InstagramDownloadModel(type,url));
                }
                model.setStatus(Constant.FETCH_SUCCESSFUL);
                model.setModelList(modelList);
            }
            else if (jsonObject.has("error"))
            {
                model.setStatus(Constant.STORY_EXPIRED);
                model.setModelList(modelList);
            }
            else
            {
                model.setStatus(Constant.UNKNOWN_ERROR);
                model.setModelList(modelList);
            }

        } catch (Exception e) {
            model.setStatus(Constant.UNKNOWN_ERROR);
            model.setModelList(modelList);
            e.printStackTrace();
            Log.i(TAG, "getReels: "+e);
        }
        return model;
    }

    public ResponseModel getReels(String jsonData)
    {
        Log.i(TAG, "getReels: "+jsonData);
        ResponseModel model = new ResponseModel();
        List<InstagramDownloadModel> modelList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("reels"))
            {
                JSONArray stories = jsonObject.getJSONArray("reels");
                for(int i=0;i<stories.length();i++)
                {
                    String type = stories.getJSONObject(i).getString("type");
                    String url = stories.getJSONObject(i).getString("url");
                    Log.i(TAG, "getReels: url"+url);
                    modelList.add(new InstagramDownloadModel(type,url));
                }
                model.setStatus(Constant.FETCH_SUCCESSFUL);
                model.setModelList(modelList);
            }
            else if (jsonObject.has("message"))
            {
                model.setStatus(Constant.COOKIE_EXPIRED);
                model.setModelList(modelList);
            }
            else
            {
                Log.i(TAG, "getReels: "+jsonData);
                model.setStatus(Constant.UNKNOWN_ERROR);
                model.setModelList(modelList);
            }

        } catch (Exception e) {
            model.setStatus(Constant.UNKNOWN_ERROR);
            model.setModelList(modelList);
            e.printStackTrace();
            Log.i(TAG, "getReels: "+e);
        }
        return model;
    }


    public ResponseModel getPosts(String jsonData)
    {
        ResponseModel model = new ResponseModel();
        List<InstagramDownloadModel> modelList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has("posts"))
            {
                JSONArray stories = jsonObject.getJSONArray("posts");
                for(int i=0;i<stories.length();i++)
                {
                    String type = stories.getJSONObject(i).getString("type");
                    String url = stories.getJSONObject(i).getString("url");
                    Log.i(TAG, "getPosts: url"+url);
                    modelList.add(new InstagramDownloadModel(type,url));
                }
                model.setStatus(Constant.FETCH_SUCCESSFUL);
                model.setModelList(modelList);
            }
            else if (jsonObject.has("message"))
            {
                model.setStatus(Constant.COOKIE_EXPIRED);
                model.setModelList(modelList);
            }
            else
            {
                model.setStatus(Constant.UNKNOWN_ERROR);
                model.setModelList(modelList);
            }

        } catch (Exception e) {
            model.setStatus(Constant.UNKNOWN_ERROR);
            model.setModelList(modelList);
            e.printStackTrace();
            Log.i(TAG, "getReels: "+e);
        }
        return model;
    }



}
