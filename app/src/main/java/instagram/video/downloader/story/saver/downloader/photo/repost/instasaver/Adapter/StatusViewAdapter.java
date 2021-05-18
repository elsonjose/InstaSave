package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.Status;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.ViewHolder.StatusViewViewHolder;

public class StatusViewAdapter extends RecyclerView.Adapter<StatusViewViewHolder> {

    private static final String TAG = "StatusViewAdapterLogs";
    List<Status> statusList;
    WeakReference<Context> context;
    MediaController controller;

    public StatusViewAdapter() {
    }

    public StatusViewAdapter(List<Status> statusList, Context context) {
        this.statusList = statusList;
        this.context = new WeakReference<Context>(context);
        controller = new MediaController(this.context.get());
    }

    @NonNull
    @Override
    public StatusViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StatusViewViewHolder(LayoutInflater.from(context.get()).inflate(R.layout.status_view_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewViewHolder holder, int position) {

        if (statusList.get(position).isVideo()) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.photoView.setVisibility(View.GONE);
            holder.videoView.setVideoPath(statusList.get(position).getFilePath());
            holder.videoView.setMediaController(controller);

        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.photoView.setVisibility(View.VISIBLE);
            Glide.with(context.get()).load(statusList.get(position).getFilePath()).into(holder.photoView);
        }
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }
}
