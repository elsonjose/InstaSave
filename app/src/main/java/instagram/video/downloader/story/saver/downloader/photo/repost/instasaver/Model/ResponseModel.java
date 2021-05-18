package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model;

import java.util.List;

public class ResponseModel {

    int status;
    List<InstagramDownloadModel> modelList;

    public ResponseModel() {
    }

    public ResponseModel(int status, List<InstagramDownloadModel> modelList) {
        this.status = status;
        this.modelList = modelList;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setModelList(List<InstagramDownloadModel> modelList) {
        this.modelList = modelList;
    }

    public int getStatus() {
        return status;
    }

    public List<InstagramDownloadModel> getModelList() {
        return modelList;
    }
}
