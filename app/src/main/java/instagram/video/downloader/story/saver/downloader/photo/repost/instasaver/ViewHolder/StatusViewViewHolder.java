package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.ViewHolder;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import uk.co.senab.photoview.PhotoView;

public class StatusViewViewHolder extends RecyclerView.ViewHolder {
    public PhotoView photoView;
    public VideoView videoView;

    public StatusViewViewHolder(@NonNull View itemView) {
        super(itemView);
        photoView = itemView.findViewById(R.id.status_photo_view);
        videoView = itemView.findViewById(R.id.status_video_view);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

    }
}
