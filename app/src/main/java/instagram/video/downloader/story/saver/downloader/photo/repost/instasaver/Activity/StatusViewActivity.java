package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.util.List;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Adapter.StatusViewAdapter;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.MainActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.Status;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;

public class StatusViewActivity extends AppCompatActivity {
    private static final String TAG = "StatusViewActivity";
   ViewPager2 viewPager;
    List<Status> statusList;
    int openPosition;
    boolean VIEWING_SAVED_STATUS=false;
    TextView statusInfoTextView;
    ImageButton shareStatusBtn,deleteStatusBtn,backBtn, repostWhatsappBtn, repostInstagramBtn;
    RelativeLayout statusActionWrapper;

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            if(statusInfoTextView!=null && statusActionWrapper!=null)
            {
                TransitionManager.beginDelayedTransition(statusActionWrapper,new AutoTransition());
                statusInfoTextView.setVisibility(View.GONE);
            }
        }
    };
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_status_view);
        statusList = getIntent().getExtras().getParcelableArrayList(Constant.STATUS_PASS_KEY);
        openPosition = getIntent().getExtras().getInt(Constant.STATUS_PASS_POSITION);
        VIEWING_SAVED_STATUS = getIntent().getExtras().getBoolean(Constant.VIEWING_SAVED_STATUS);

        viewPager = findViewById(R.id.view_pager);

        statusInfoTextView = findViewById(R.id.status_info_textView);
        deleteStatusBtn = findViewById(R.id.status_delete_btn);
        shareStatusBtn = findViewById(R.id.status_share_btn);
        repostWhatsappBtn = findViewById(R.id.status_whatsapp_btn);
        repostInstagramBtn = findViewById(R.id.status_instagram_btn);
        backBtn = findViewById(R.id.status_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        statusActionWrapper = findViewById(R.id.status_btn_wrapper);

        deleteStatusBtn.setVisibility(View.VISIBLE);

        handler = new Handler();

        StatusViewAdapter statusViewAdapter = new StatusViewAdapter(statusList, StatusViewActivity.this);
        viewPager.setAdapter(statusViewAdapter);
        viewPager.setCurrentItem(openPosition,false);


        repostInstagramBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = FileProvider.getUriForFile(StatusViewActivity.this, "instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.fileprovider",new File(statusList.get(viewPager.getCurrentItem()).getFilePath()));
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.instagram.android");
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                whatsappIntent.setType("image/jpeg");
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    FirebaseLogger.logErrorData("StatusViewActivity  repostInstagramBtn.setOnClickListener",ex.toString());
                    showActionResult("Instagram not installed.");
                }
            }
        });


        repostWhatsappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = FileProvider.getUriForFile(StatusViewActivity.this, "instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.fileprovider",new File(statusList.get(viewPager.getCurrentItem()).getFilePath()));
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                whatsappIntent.setType("image/jpeg");
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    FirebaseLogger.logErrorData("StatusViewActivity  repostWhatsappBtn.setOnClickListener",ex.toString());
                    showActionResult("Whatsapp not installed.");
                }
            }
        });

        deleteStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = viewPager.getCurrentItem();
                if(new File(statusList.get(currentPos).getFilePath()).delete())
                {
                    showActionResult("File deleted");
                    try {
                        MainActivity.getMainActivity().loadSavedInstagramContents();
                    }
                    catch (Exception e)
                    {
                        FirebaseLogger.logErrorData("StatusViewActivity deleteStatusBtn.setOnClickListener",e.toString());
                        Log.i(TAG, "onClick: "+e);
                    }
                }
                else
                {
                    if(!new File(statusList.get(currentPos).getFilePath()).exists())
                    {
                        showActionResult("Already deleted");
                    }
                    else
                    {
                        showActionResult("Some error occurred");
                    }
                }
            }
        });
        shareStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = viewPager.getCurrentItem();
                if(new File(statusList.get(currentPos).getFilePath()).exists())
                {
                    showActionResult("Preparing for share");
                    Uri uri = FileProvider.getUriForFile(StatusViewActivity.this, "instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.fileprovider", new File(statusList.get(currentPos).getFilePath()));
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
//                    intent.putExtra(Intent.EXTRA_TEXT, "Shared with Swift. Get your now http://bit.ly/3j8CQFI");
                    intent.putExtra(Intent.EXTRA_TEXT, "Shared with InstaSave");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if(statusList.get(currentPos).isVideo())
                    {
                        intent.setType("video/*");
                    }
                    else
                    {
                        intent.setType("image/*");
                    }
                    startActivity(Intent.createChooser(intent, "Sharing via"));
                }
                else
                {
                    showActionResult("File does not exist");
                }
            }
        });
    }

    private void showActionResult(String result) {
        statusInfoTextView.setText(result);
        TransitionManager.beginDelayedTransition(statusActionWrapper,new AutoTransition());
        statusInfoTextView.setVisibility(View.VISIBLE);
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,1500);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}