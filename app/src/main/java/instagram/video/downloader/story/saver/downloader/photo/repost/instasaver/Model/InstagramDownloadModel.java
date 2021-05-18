package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class InstagramDownloadModel implements Parcelable {
    String type;
    String url;
    long time;

    public InstagramDownloadModel(String type, String url) {
        this.type = type;
        this.url = url;
    }

    public InstagramDownloadModel(String type, String url, long time) {
        this.type = type;
        this.url = url;
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public long getTime() {
        return time;
    }

    public static Creator<InstagramDownloadModel> getCREATOR() {
        return CREATOR;
    }

    protected InstagramDownloadModel(Parcel in) {
        type = in.readString();
        url = in.readString();
        time = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(url);
        dest.writeLong(time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstagramDownloadModel> CREATOR = new Creator<InstagramDownloadModel>() {
        @Override
        public InstagramDownloadModel createFromParcel(Parcel in) {
            return new InstagramDownloadModel(in);
        }

        @Override
        public InstagramDownloadModel[] newArray(int size) {
            return new InstagramDownloadModel[size];
        }
    };
}
