package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class InstagramStoryModel implements Parcelable {
    String name;
    String profileUrl;
    String uid;
    List<InstagramDownloadModel> storyList=new ArrayList<>();

    public InstagramStoryModel(String name, String profileUrl, String uid, List<InstagramDownloadModel> storyList) {
        this.name = name;
        this.profileUrl = profileUrl;
        this.uid = uid;
        this.storyList = storyList;
    }

    protected InstagramStoryModel(Parcel in) {
        name = in.readString();
        profileUrl = in.readString();
        uid = in.readString();
        storyList = in.createTypedArrayList(InstagramDownloadModel.CREATOR);
    }

    public static final Creator<InstagramStoryModel> CREATOR = new Creator<InstagramStoryModel>() {
        @Override
        public InstagramStoryModel createFromParcel(Parcel in) {
            return new InstagramStoryModel(in);
        }

        @Override
        public InstagramStoryModel[] newArray(int size) {
            return new InstagramStoryModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(profileUrl);
        dest.writeString(uid);
        dest.writeTypedList(storyList);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<InstagramDownloadModel> getStoryList() {
        return storyList;
    }

    public void setStoryList(List<InstagramDownloadModel> storyList) {
        this.storyList = storyList;
    }
}

