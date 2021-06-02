package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import java.lang.ref.WeakReference;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity.InstagramLoginActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.MainActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;

public class Notification extends ContextWrapper {


    WeakReference<Context> context;
    NotificationManager manager;
    static final String Channel_ID="130",Channel_NAME="instasaveD";


    public Notification(Context context) {
        super(context);
        this.context = new WeakReference<Context>(context);
    }

    private void CreateChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(Channel_ID,Channel_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setImportance(NotificationManager.IMPORTANCE_MIN);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(channel);
        }
    }

    public NotificationManager getManager() {

        if(manager==null)
        {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotificationBuilder(String title, String body)
    {
        return new android.app.Notification.Builder(getApplicationContext(),Channel_ID).setContentText(body)
                .setContentTitle(title)
                .setProgress(100,0,true)
                .setSmallIcon(R.mipmap.ic_launcher).setOnlyAlertOnce(true);
    }


    public void showInstagramLogin(int notificationID,String title, String desc){

        Intent intent = new Intent(context.get(), InstagramLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context.get(), (int) (Math.random() * 100),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager=(NotificationManager)context.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CreateChannel();
            String channelID = "ID_503";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Private", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(context.get(), channelID)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context.get());
            builder .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,builder.build());
        }

    }


    public void displayInstaDownload(int notificationID,String title, String desc){

        Intent intent = new Intent(context.get(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context.get(), (int) (Math.random() * 100),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager=(NotificationManager)context.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CreateChannel();
            String channelID = "ID_506";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Conversion", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(context.get(), channelID)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context.get());
            builder .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,builder.build());
        }

    }

    public void displayVersionUpdate(int notificationID,String title, String desc){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp.status.saver.downloader.share"));
        PendingIntent contentIntent = PendingIntent.getActivity(context.get(), (int) (Math.random() * 100),
                browserIntent, 0);

        NotificationManager notificationManager=(NotificationManager)context.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CreateChannel();
            String channelID = "ID_506";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Conversion", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(context.get(), channelID)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context.get());
            builder .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,builder.build());
        }

    }

    public void displayReviewNotification(int notificationID,String title, String desc){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp.status.saver.downloader.share"));
        PendingIntent contentIntent = PendingIntent.getActivity(context.get(), (int) (Math.random() * 100),
                browserIntent, 0);

        NotificationManager notificationManager=(NotificationManager)context.get().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CreateChannel();
            String channelID = "ID_506";
            NotificationChannel notificationChannel = new NotificationChannel(channelID,"Conversion", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            android.app.Notification.Builder notificationBuilder = new android.app.Notification.Builder(context.get(), channelID)
                    .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,notificationBuilder.build());
        }
        else
        {
            NotificationCompat.Builder builder=new NotificationCompat.Builder(context.get());
            builder .setContentTitle(title)
                    .setContentText(desc)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(context.get().getColor(R.color.white))
                    .setAutoCancel(true);
            notificationManager.notify(notificationID,builder.build());
        }

    }



}
