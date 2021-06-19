package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.InstaLink;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.LinkDatabase;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.MainActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Notification;

public class InstaDownloadWorker extends Worker {

    private static final String TAG = "InstaDownloadWorker";
    Notification helper;
    android.app.Notification.Builder builderOreo;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    int instaDownloadNotificationID;
    LinkDatabase linkDatabase;
    Context context;

    public InstaDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        linkDatabase = Room.databaseBuilder(context, LinkDatabase.class, Constant.LINK_DB).fallbackToDestructiveMigration().build();
        List<InstaLink> instaLinkList = linkDatabase.getLinkDao().getAllLinks();
//        Log.i(TAG, "doWork: size " + instaLinkList.size());
//        linkDatabase.getLinkDao().deleteAllLinks();
        if(instaLinkList.size()>0)
        {
            downloadInstaPost(instaLinkList);
        }
        return Result.success();

    }

    private void downloadInstaPost(List<InstaLink> instaLinkList) {

        Intent broadcastIntent=new Intent();
        broadcastIntent.setAction("com.refresh.screen");
        broadcastIntent.putExtra(Constant.BROADCAST_ACTION,Constant.DOWNLOAD_STARTED);
        context.sendBroadcast(broadcastIntent);

        int position = -1;
        for (int i = 0; i < instaLinkList.size(); i++) {
            if (instaLinkList.get(i).isQueued() && !instaLinkList.get(i).isDownloaded() && !instaLinkList.get(i).isFailed()) {
                position = i;
                break;
            }
        }
        if (position != -1) {

            int downloadCount = linkDatabase.getLinkDao().getDownloadedLinksCount();
            int queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
            int displayDownloadCount = downloadCount + 1;
            int displayTotalCount = downloadCount + queuedCount;


//            Log.i(TAG, "downloadInstaPost: link" + instaLinkList.get(position).getUrl());
            String downloadUrl = instaLinkList.get(position).getUrl();
            String contentType = instaLinkList.get(position).getType();
//            Log.i(TAG, "downloadInstaPost: contentType" + contentType);
            if (downloadUrl != null && !TextUtils.isEmpty(downloadUrl)) {
                instaDownloadNotificationID = (int) System.currentTimeMillis();
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(context, (int) (Math.random() * 100),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    helper = new Notification(context);
                    builderOreo = helper.getNotificationBuilder("Downloading Content", "Starting download");
                    builderOreo.setContentIntent(contentIntent);
                    helper.getManager().notify(instaDownloadNotificationID, builderOreo.build());
                } else {

                    notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    builder = new NotificationCompat.Builder(getApplicationContext(), String.valueOf(instaDownloadNotificationID));
                    builder.setContentTitle("Downloading Content")
                            .setContentText("Starting download")
                            .setProgress(100, 0, true)
                            .setSmallIcon(R.drawable.ic_download)
                            .setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.DEFAULT_ALL);
                }
                try {
                    publishTitle("Downloading Content (" + displayDownloadCount + "/" + displayTotalCount + ")");
                    if (contentType.equals(Constant.GRAPH_VIDEO)) {

                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                        {
                            URL _url = new URL(downloadUrl);
                            HttpURLConnection con = (HttpURLConnection) _url.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();
                            int file_size = con.getContentLength();

                            String fileName = "Instagram_content_"+System.currentTimeMillis()+".mp4";

                            File outputFile = new File(context.getFilesDir()+fileName);
                            if (!outputFile.exists()) {
                                outputFile.createNewFile();
                            }

                            FileOutputStream fos = new FileOutputStream(outputFile);
                            int status = con.getResponseCode();
                            if (status == HttpURLConnection.HTTP_OK) {
                                InputStream is = con.getInputStream();
                                byte[] buffer = new byte[4096];
                                int len1 = 0;
                                long total = 0;
                                while ((len1 = is.read(buffer)) != -1) {
                                    total += len1;
                                    int progress = (int) (total * 100 / file_size);
                                    publishProgress("Please wait until video is downloaded", String.valueOf(progress));
                                    int newQueuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                    if (newQueuedCount != queuedCount) {
                                        downloadCount = linkDatabase.getLinkDao().getDownloadedLinksCount();
                                        queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                        displayDownloadCount = downloadCount + 1;
                                        displayTotalCount = downloadCount + queuedCount;
                                        publishTitle("Downloading Content (" + displayDownloadCount + "/" + displayTotalCount + ")");
                                    }
                                    fos.write(buffer, 0, len1);

                                }
                                fos.close();
                                is.close();
                                con.disconnect();

                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Video.Media.TITLE, fileName);
                                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "video/mp4");
                                values.put(MediaStore.Video.Media.DESCRIPTION, "");
                                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis());
                                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES+File.separator+"IGet");
                                values.put(MediaStore.Files.FileColumns.IS_PENDING, true);
                                Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                                ContentResolver cr = context.getContentResolver();

                                InputStream input = cr.openInputStream(Uri.fromFile(outputFile));
                                OutputStream outputStream = cr.openOutputStream(uri);

                                byte[] buffer_1 = new byte[4 * 1024]; // or other buffer size
                                int read;

                                while ((read = input.read(buffer_1)) != -1) {
                                    outputStream.write(buffer_1, 0, read);
                                }
                                outputStream.flush();
                                values.put(MediaStore.Video.Media.IS_PENDING, false);
                                context.getContentResolver().update(uri, values, null, null);

//                                Log.i(TAG, "onCreate: download complete");
//                                Log.i(TAG, "onCreate: uri " + uri);
                                instaLinkList.get(position).setDownloaded(true);
                                instaLinkList.get(position).setQueued(false);
                                InstaLink link = instaLinkList.get(position);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));

                                SharedPreferences appDataPref = context.getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
                                int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
                                appDataPrefEditor.putInt(Constant.DOWNLOADED_COUNT,downloadedCount+1);
                                appDataPrefEditor.apply();

                            }
                            else {
                                InstaLink link = instaLinkList.get(position);
                                link.setFailed(true);
                                link.setDownloaded(false);
                                link.setQueued(false);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));

                            }
                        }
                        else
                        {

                            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "IGet" + File.separator+ "Instagram_content_" + System.currentTimeMillis() + ".mp4";
                            if (!new File(fileName).getParentFile().exists())
                                new File(fileName).getParentFile().mkdirs();
                            if (!new File(fileName).exists())
                                new File(fileName).createNewFile();

//                            if(new File(fileName).exists())
//                            {
//                                Log.i(TAG, "downloadInstaPost: file created");
//                            }

                            URL _url = new URL(downloadUrl);
                            HttpURLConnection con = (HttpURLConnection) _url.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();
                            int file_size = con.getContentLength();
                            File outputFile = new File(fileName);
                            if (!outputFile.exists()) {
                                outputFile.createNewFile();
                            }

                            FileOutputStream fos = new FileOutputStream(outputFile);
                            int status = con.getResponseCode();
                            if (status == HttpURLConnection.HTTP_OK) {
                                InputStream is = con.getInputStream();
                                byte[] buffer = new byte[4096];
                                int len1 = 0;
                                long total = 0;
                                while ((len1 = is.read(buffer)) != -1) {
                                    total += len1;
                                    int progress = (int) (total * 100 / file_size);
                                    publishProgress("Please wait until video is downloaded", String.valueOf(progress));
                                    int newQueuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                    if (newQueuedCount != queuedCount) {
                                        downloadCount = linkDatabase.getLinkDao().getDownloadedLinksCount();
                                        queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                        displayDownloadCount = downloadCount + 1;
                                        displayTotalCount = downloadCount + queuedCount;
                                        publishTitle("Downloading Content (" + displayDownloadCount + "/" + displayTotalCount + ")");
                                    }
                                    fos.write(buffer, 0, len1);
                                }

                                fos.close();
                                is.close();
                                con.disconnect();

                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Video.Media.TITLE, fileName);
                                values.put(MediaStore.Video.Media.DESCRIPTION, "");
                                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "video/mp4");
                                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis());
                                values.put(MediaStore.Video.Media.DATA, outputFile.getAbsolutePath());
                                ContentResolver cr = context.getContentResolver();
                                cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                                instaLinkList.get(position).setDownloaded(true);
                                instaLinkList.get(position).setQueued(false);
                                InstaLink link = instaLinkList.get(position);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));

                                SharedPreferences appDataPref = context.getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
                                int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
                                appDataPrefEditor.putInt(Constant.DOWNLOADED_COUNT,downloadedCount+1);
                                appDataPrefEditor.apply();

                            } else {
                                InstaLink link = instaLinkList.get(position);
                                link.setFailed(true);
                                link.setDownloaded(false);
                                link.setQueued(false);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));

                            }
                        }

                        downloadInstaPost(instaLinkList);
                    }
                    else {

                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {

//                            Log.i(TAG, "downloadInstaPost: image android Q or higher");

                            URL _url = new URL(downloadUrl);
                            HttpURLConnection con = (HttpURLConnection) _url.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();
                            int file_size = con.getContentLength();

                            String fileName = "Instagram_content_" + System.currentTimeMillis() + ".jpg";

                            File outputFile = new File(context.getFilesDir() + fileName);
                            if (!outputFile.exists()) {
                                outputFile.createNewFile();
//                                if (outputFile.exists())
//                                {
//                                    Log.i(TAG, "downloadInstaPost: file created");
//                                }
                            }


                            FileOutputStream fos = new FileOutputStream(outputFile);
                            int status = con.getResponseCode();
                            if (status == HttpURLConnection.HTTP_OK) {
                                InputStream is = con.getInputStream();
                                byte[] buffer = new byte[4096];
                                int len1 = 0;
                                long total = 0;
                                while ((len1 = is.read(buffer)) != -1) {
                                    total += len1;
                                    int progress = (int) (total * 100 / file_size);
                                    publishProgress("Please wait until photo is downloaded", String.valueOf(progress));
                                    int newQueuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                    if (newQueuedCount != queuedCount) {
                                        downloadCount = linkDatabase.getLinkDao().getDownloadedLinksCount();
                                        queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                        displayDownloadCount = downloadCount + 1;
                                        displayTotalCount = downloadCount + queuedCount;
                                        publishTitle("Downloading Content (" + displayDownloadCount + "/" + displayTotalCount + ")");
                                    }
                                    fos.write(buffer, 0, len1);

                                }
                                fos.close();
                                is.close();
                                con.disconnect();

                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.TITLE, fileName);
                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                values.put(MediaStore.Images.Media.DESCRIPTION, "");
                                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+File.separator+"IGet");
                                values.put(MediaStore.Images.Media.IS_PENDING, true);
                                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                ContentResolver cr = context.getContentResolver();

                                InputStream input = cr.openInputStream(Uri.fromFile(outputFile));
                                OutputStream outputStream = cr.openOutputStream(uri);

                                byte[] buffer_1 = new byte[4 * 1024]; // or other buffer size
                                int read;

                                while ((read = input.read(buffer_1)) != -1) {
                                    outputStream.write(buffer_1, 0, read);
                                }
                                outputStream.flush();
                                values.put(MediaStore.Images.Media.IS_PENDING, false);
                                context.getContentResolver().update(uri, values, null, null);
                                instaLinkList.get(position).setDownloaded(true);
                                instaLinkList.get(position).setQueued(false);
                                InstaLink link = instaLinkList.get(position);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));

                                SharedPreferences appDataPref = context.getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
                                int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
                                appDataPrefEditor.putInt(Constant.DOWNLOADED_COUNT,downloadedCount+1);
                                appDataPrefEditor.apply();

                            }
                            else
                            {
                                InstaLink link = instaLinkList.get(position);
                                link.setFailed(true);
                                link.setDownloaded(false);
                                link.setQueued(false);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));
                            }
                        }
                        else
                        {

                            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator+"IGet" + File.separator+ "Instagram_content_" + System.currentTimeMillis() + ".jpg";

                            if (!new File(fileName).getParentFile().exists())
                                new File(fileName).getParentFile().mkdirs();
                            if (!new File(fileName).exists())
                                new File(fileName).createNewFile();

//                            if(new File(fileName).exists())
//                            {
//                                Log.i(TAG, "downloadInstaPost: file created");
//                            }

                            URL _url = new URL(downloadUrl.trim());
                            HttpURLConnection con = (HttpURLConnection) _url.openConnection();
                            con.setRequestMethod("GET");
                            con.connect();
                            int file_size = con.getContentLength();
                            File outputFile = new File(fileName);
                            if (!outputFile.exists()) {
                                outputFile.createNewFile();
                            }
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            int status = con.getResponseCode();
                            if (status == HttpURLConnection.HTTP_OK) {
                                InputStream is = con.getInputStream();
                                byte[] buffer = new byte[1024];
                                int len1 = 0;
                                long total = 0;
                                while ((len1 = is.read(buffer)) != -1) {
                                    total += len1;
                                    int progress = (int) (total * 100 / file_size);
                                    publishProgress("Please wait until photo is downloaded", String.valueOf(progress));
                                    int newQueuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                    if (newQueuedCount != queuedCount) {
                                        downloadCount = linkDatabase.getLinkDao().getDownloadedLinksCount();
                                        queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
                                        displayDownloadCount = downloadCount + 1;
                                        displayTotalCount = downloadCount + queuedCount;
                                        publishTitle("Downloading Content (" + displayDownloadCount + "/" + displayTotalCount + ")");
                                    }
                                    fos.write(buffer, 0, len1);
                                }
                                fos.close();
                                is.close();
                                con.disconnect();
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.TITLE, fileName);
                                values.put(MediaStore.Images.Media.DESCRIPTION, "");
                                values.put(MediaStore.Images.Media.DATA, outputFile.getAbsolutePath());
                                ContentResolver cr = context.getContentResolver();
                                cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                instaLinkList.get(position).setDownloaded(true);
                                instaLinkList.get(position).setQueued(false);
                                InstaLink link = instaLinkList.get(position);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(),  link.getType()));

                                SharedPreferences appDataPref = context.getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
                                int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
                                appDataPrefEditor.putInt(Constant.DOWNLOADED_COUNT,downloadedCount+1);
                                appDataPrefEditor.apply();

                            }else
                            {
                                InstaLink link = instaLinkList.get(position);
                                link.setFailed(true);
                                link.setDownloaded(false);
                                link.setQueued(false);
                                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));
                            }
                        }
                        downloadInstaPost(instaLinkList);

                    }
                } catch (Exception e) {
//                    Log.i(TAG, "instaDownloadWorker" + e);
                    instaLinkList.get(position).setDownloaded(false);
                    instaLinkList.get(position).setQueued(false);
                    instaLinkList.get(position).setFailed(true);
                    InstaLink link = instaLinkList.get(position);
                    linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(),  link.getType()));
                    downloadInstaPost(instaLinkList);
                }


            } else {
//                Log.i(TAG, "downloadUrl empty");
//                Log.i(TAG, "downloadInstaPost: line 248");
                instaLinkList.get(position).setDownloaded(false);
                instaLinkList.get(position).setQueued(false);
                instaLinkList.get(position).setFailed(true);
                InstaLink link = instaLinkList.get(position);
                linkDatabase.getLinkDao().updateLinkDatabase(new InstaLink(link.getUrl(), link.getLinkID(), link.isDownloaded(), link.isQueued(), link.isFailed(), link.getType()));
                downloadInstaPost(instaLinkList);
            }
        } else {
            List<InstaLink> newInstaLinkList = linkDatabase.getLinkDao().getAllLinks();
            int queuedCount = linkDatabase.getLinkDao().getQueuedLinksCount();
            for(InstaLink link:linkDatabase.getLinkDao().getAllLinks())
//                Log.i(TAG, "link: "+link.getUrl()+" failed "+link.isFailed()+" downloaded "+ link.isDownloaded()+" queued "+link.isQueued());

            if (queuedCount == 0) {

                SharedPreferences appDataPref = context.getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
                int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
//                Log.i(TAG, "total downloaded count: "+downloadedCount);

                new Notification(context).displayInstaDownload(Constant.instaDownloadNotificationID, "Download Complete", "Tap to view contents");

                Intent completeBroadcastIntent =new Intent();
                completeBroadcastIntent.setAction("com.refresh.screen");
                completeBroadcastIntent.putExtra(Constant.BROADCAST_ACTION,Constant.DOWNLOAD_COMPLETE);
                context.sendBroadcast(completeBroadcastIntent);

                linkDatabase.getLinkDao().deleteAllFailedLinks();
            } else {
                downloadInstaPost(newInstaLinkList);
            }

        }


    }

    private void publishProgress(String s, String progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && helper != null && builderOreo != null) {
            builderOreo.setContentText(s).setProgress(100, Integer.parseInt(progress), false);
            helper.getManager().notify(Constant.instaDownloadNotificationID, builderOreo.build());
        } else {
            if (builder != null && notificationManager != null) {
                builder.setContentText(s).setProgress(100, Integer.parseInt(progress), false);
                notificationManager.notify(Constant.instaDownloadNotificationID, builder.build());
            }
        }
    }

    private void publishTitle(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && helper != null && builderOreo != null) {
            builderOreo.setContentTitle(s);
            helper.getManager().notify(Constant.instaDownloadNotificationID, builderOreo.build());
        } else {
            if (builder != null && notificationManager != null) {
                builder.setContentTitle(s);
                notificationManager.notify(Constant.instaDownloadNotificationID, builder.build());
            }
        }
    }



}
