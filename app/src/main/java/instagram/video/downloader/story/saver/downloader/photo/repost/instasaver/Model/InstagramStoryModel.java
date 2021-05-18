package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class InstagramStoryModel implements Parcelable {
    String name;
    String profileUrl;
    List<InstagramDownloadModel> storyList=new ArrayList<>();

    public InstagramStoryModel(String name, String profileUrl, List<InstagramDownloadModel> storyList) {
        this.name = name;
        this.profileUrl = profileUrl;
        this.storyList = storyList;
    }

    protected InstagramStoryModel(Parcel in) {
        name = in.readString();
        profileUrl = in.readString();
        in.readList(storyList, InstagramDownloadModel.class.getClassLoader());

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

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public List<InstagramDownloadModel> getStoryList() {
        return storyList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(profileUrl);
        parcel.writeList(storyList);
    }
}

