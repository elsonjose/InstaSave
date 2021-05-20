package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseLogger {

    public static void logErrorData(String errorLoc, Exception e)
    {
        Map userLog = new HashMap();
        userLog.put("os", Build.VERSION.SDK_INT);
        userLog.put("device", Build.DEVICE);
        userLog.put("model", Build.MODEL);
        userLog.put("product", Build.PRODUCT);
        userLog.put("appVersion","3-build");
        userLog.put("errorLoc",errorLoc);
        userLog.put("error",e);
        FirebaseDatabase.getInstance().getReference().child("Crash").push().setValue(userLog);
    }
}
