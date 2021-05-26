package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.os.Build;
import android.text.format.DateFormat;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseLogger {

    public static void logErrorData(String errorLoc, String e)
    {
        Date date = new Date(System.currentTimeMillis());
        String dateformat = DateFormat.format("dd-MM-yyyy", date).toString();

        Map userLog = new HashMap();
        userLog.put("os", Build.VERSION.SDK_INT);
        userLog.put("device", Build.DEVICE);
        userLog.put("model", Build.MODEL);
        userLog.put("errorLoc",errorLoc);
        userLog.put("error",e);
        FirebaseDatabase.getInstance().getReference().child("Crash").child("v6").child(dateformat).push().setValue(userLog);
    }
}
