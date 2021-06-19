package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Util {
    WeakReference<Context> context;

    private static final String TAG = "UtilLogs";
    public Util(Context context) {
        this.context = new WeakReference<Context>(context);
    }


    public WorkInfo.State getStateOfWork(String tag) {
        try {
            if (WorkManager.getInstance(context.get()).getWorkInfosForUniqueWork(tag).get().size() > 0) {
                return WorkManager.getInstance(context.get()).getWorkInfosForUniqueWork(tag).get().get(0).getState();
            } else {
                return WorkInfo.State.CANCELLED;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        }
    }



    public boolean isConntectedToNet() {
        ConnectivityManager cm = (ConnectivityManager) context.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public String getDate(String timestamp)
    {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        String dateformat = DateFormat.format("dd-MMM-yy", date).toString();
        return dateformat;
    }

    public String getDateAndTime(String timestamp)
    {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        return DateFormat.format("dd-MMM-yy", date).toString()+", "+DateFormat.format("hh:mm aa", date).toString();
    }

    public boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
