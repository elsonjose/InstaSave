package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;

@Dao
public interface LinkDao {

    @Insert
    public long addLink(InstaLink instaLink);

    @Update
    public void updateLinkDatabase(InstaLink instaLink);

    @Delete
    public void removeLink(InstaLink instaLink);

    @Query("select * from "+ Constant.TABLE_NAME+" where isFailed =0")
    public List<InstaLink> getAllLinks();

    @Query("select * from "+ Constant.TABLE_NAME+" where isFailed =1")
    public List<InstaLink> getAllFailedLinks();

    @Query("select count(*) from "+ Constant.TABLE_NAME+" where isDownloaded =1 and isFailed =0")
    public int getDownloadedLinksCount();

    @Query("select count(*) from "+ Constant.TABLE_NAME+" where isQueued =1 and isFailed =0")
    public int getQueuedLinksCount();

    @Query("select exists(select * from "+Constant.TABLE_NAME +" where url ==:url)")
    public boolean checkInstaLinkExists(String url);

    @Query("DELETE FROM "+Constant.TABLE_NAME +" where url ==:url")
    public int deleteInstaLink(String url);

    @Query("DELETE FROM "+Constant.TABLE_NAME)
    public void deleteAllLinks();

    @Query("DELETE FROM "+Constant.TABLE_NAME+" where isFailed=0")
    public void deleteAllFailedLinks();

    @Query("select count(*) from "+Constant.TABLE_NAME+" where isFailed =1")
    public int getFailedCount();


    @Query("UPDATE "+Constant.TABLE_NAME+" SET isFailed =1 WHERE linkID LIKE :id")
    public int linkFailed(long id);

}
