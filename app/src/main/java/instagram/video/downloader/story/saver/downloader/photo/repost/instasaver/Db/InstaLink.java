package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;

@Entity(tableName = Constant.TABLE_NAME)
public class InstaLink {

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "linkID")
    @PrimaryKey(autoGenerate = true)
    private long linkID;

    @ColumnInfo(name = "isDownloaded")
    private boolean isDownloaded;

    @ColumnInfo(name = "isQueued")
    private boolean isQueued;

    @ColumnInfo(name = "isFailed")
    private boolean isFailed;


    @ColumnInfo(name = "type")
    private String type;

    @Ignore
    public InstaLink() {
    }

    public InstaLink(String url, long linkID, boolean isDownloaded, boolean isQueued, boolean isFailed, String type) {
        this.url = url;
        this.linkID = linkID;
        this.isDownloaded = isDownloaded;
        this.isQueued = isQueued;
        this.isFailed = isFailed;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLinkID() {
        return linkID;
    }

    public void setLinkID(long linkID) {
        this.linkID = linkID;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean queued) {
        isQueued = queued;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }
}
