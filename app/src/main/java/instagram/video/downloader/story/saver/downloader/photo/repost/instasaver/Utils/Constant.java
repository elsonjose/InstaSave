package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.os.Environment;

public class Constant {

    public static final String STATUS_PASS_KEY="status";
    public static final String STATUS_PASS_POSITION = "Position";

    public static final String THEME_PREF="Theme.Pref";
    public static final String THEME_KEY ="theme";
    public static final String THEME_VAL_SYSTEM="system";
    public static final String THEME_VAL_LIGHT="light";
    public static final String THEME_VAL_DARK="dark";
    public static final String COPIED_URL="CopiedUrl";

    public static final int versionUpdateNotificationID =900;
    public static final int reviewNotificationID =700;
    public static final int instaDownloadNotificationID =150;
    public static final int instaLoginNotificationID =154;

    public static final String VIEWING_SAVED_STATUS = "SavedStatus";

    public static final String REVIEW_NOTIFIED = "reviewNotified";


    public static final String TABLE_NAME = "Insta_table";
    public static final String LINK_DB = "Link_DB";
    public static final String INSTA_DOWNLOAD_WORKER_TAG = "InstaWorker";

    public static final String COOKIE = "cookie";

    public static final int FETCH_SUCCESSFUL = 200;
    public static final int STORY_EXPIRED = 201;
    public static final int UNKNOWN_ERROR = 202;
    public static final int COOKIE_EXPIRED = 203;
    public static final String SHOW_HINT_INSTA_STORY = "instaHint";

    public static final String BROADCAST_ACTION = "BroadCastAction";
    public static final String DOWNLOAD_STARTED = "StartedFetch";
    public static final String DOWNLOAD_COMPLETE = "Fetched";
    public static final String STORY_LAST_CHECKED_AT = "InstaStoryCheck";
    public static final String INSTA_STORY_UPDATED = "StoryUpdated";
    public static final String INSTA_STORY_WORKER_TAG = "StoryWorker";

    public static String GRAPH_VIDEO="video";
    public static String GRAPH_IMAGE="image";
}
