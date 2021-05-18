package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {InstaLink.class},version = 1)
public abstract class LinkDatabase extends RoomDatabase {
    public abstract LinkDao getLinkDao();
}
