package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Status implements Parcelable, Comparable<Status> {
    private String filePath;
    private long lastModified;
    private boolean isSelected;
    private boolean isVideo;

    public Status(String filePath, long lastModified, boolean isSelected, boolean isVideo) {
        this.filePath = filePath;
        this.lastModified = lastModified;
        this.isSelected = isSelected;
        this.isVideo = isVideo;
    }

    protected Status(Parcel in) {
        filePath = in.readString();
        lastModified = in.readLong();
        isSelected = in.readByte() != 0;
        isVideo = in.readByte() != 0;
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    @Override
    public int compareTo(Status o) {
        return String.valueOf(o.getLastModified()).compareTo(String.valueOf(this.lastModified));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeLong(lastModified);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isVideo ? 1 : 0));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Status)
        {
            Status status = (Status) obj;
            return this.getFilePath().equals(status.filePath);
        }
        else
        {
            return false;
        }
    }
}
