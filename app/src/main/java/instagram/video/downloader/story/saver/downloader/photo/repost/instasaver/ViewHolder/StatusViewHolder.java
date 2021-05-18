package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;


public class StatusViewHolder extends RecyclerView.ViewHolder {
    public ImageView statusImageView;
    public RelativeLayout statusSelectedLayout;
    public ProgressBar statusProgressbar;
    public ImageButton statusIsVideoImageButton;
    public ImageButton statusSelectedBtn;
    public LinearLayout statusOptionWrapper;
    public ImageButton statusShareBtn,statusDownloadBtn,statusDeleteBtn;

    public StatusViewHolder(@NonNull View itemView) {
        super(itemView);
        statusImageView = itemView.findViewById(R.id.doc_imageview);
        statusSelectedLayout = itemView.findViewById(R.id.doc_checkbox_relative_layout);
        statusProgressbar = itemView.findViewById(R.id.doc_progressbar);
        statusIsVideoImageButton = itemView.findViewById(R.id.doc_video_textView);
        statusSelectedBtn = itemView.findViewById(R.id.doc_selected_btn);
        statusOptionWrapper = itemView.findViewById(R.id.photo_view_options_wrapper);
        statusShareBtn = itemView.findViewById(R.id.photo_view_share_btn);
        statusDownloadBtn = itemView.findViewById(R.id.photo_view_download_btn);
        statusDeleteBtn = itemView.findViewById(R.id.photo_view_delete_btn);
    }
}
