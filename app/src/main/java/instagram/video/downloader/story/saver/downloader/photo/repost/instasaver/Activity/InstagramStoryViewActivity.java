package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.InstaLink;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.LinkDatabase;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramDownloadModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramStoryModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Util;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker.InstaDownloadWorker;

public class InstagramStoryViewActivity extends AppCompatActivity {

    ViewPager2 storyViewPager;
    List<InstagramStoryModel> storyList = new ArrayList<>();
    int userStoryPositionOpened;
    int itemPositionWithinUserStory=0;
    private static final String TAG = "InstagramStoryViewActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        String theme = appDataPref.getString(Constant.THEME_KEY,Constant.THEME_VAL_SYSTEM);
        switch (theme)
        {
            case Constant.THEME_VAL_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                // disable manual selection
                break;
            case Constant.THEME_VAL_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                // check switch
                break;
            case Constant.THEME_VAL_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // uncheck switch
                break;
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_instagram_story_view);

        storyList = getIntent().getExtras().getParcelableArrayList(Constant.STATUS_PASS_KEY);
        userStoryPositionOpened = getIntent().getExtras().getInt(Constant.STATUS_PASS_POSITION);

//         testing data
//        storyList = new ArrayList<>();
//        userStoryPositionOpened = 1;
//        List<InstagramDownloadModel> modelList = new ArrayList<>();
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_IMAGE, "https://images.unsplash.com/photo-1593642532973-d31b6557fa68?ixid=MnwxMjA3fDF8MHxlZGl0b3JpYWwtZmVlZHwxfHx8ZW58MHx8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60",1621248002));
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_VIDEO, "http://techslides.com/demos/sample-videos/small.webm",1621222002));
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_IMAGE, "https://images.unsplash.com/photo-1620736214014-8bfe7258135f?ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw4fHx8ZW58MHx8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60",1621248002));
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_VIDEO, "http://techslides.com/demos/sample-videos/small.webm",1621258037));
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_IMAGE, "https://images.unsplash.com/photo-1620841967344-f5d88a19ae22?ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxN3x8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60",1621258037));
//        modelList.add(new InstagramDownloadModel(Constant.GRAPH_VIDEO, "http://techslides.com/demos/sample-videos/small.webm",1621222002));
//
//        storyList.add(new InstagramStoryModel("Jack", "https://images.unsplash.com/photo-1620781045428-9d4d3075f323?ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwxMnx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60", modelList));
//        storyList.add(new InstagramStoryModel("Thomas", "https://images.unsplash.com/photo-1620829139759-ec119c941826?ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHwyN3x8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60", modelList));
//        storyList.add(new InstagramStoryModel("Ryan", "https://images.unsplash.com/photo-1620794108219-aedbaded4eea?ixid=MnwxMjA3fDB8MHxlZGl0b3JpYWwtZmVlZHw0MXx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60", modelList));

        storyViewPager = findViewById(R.id.story_view_viewPager);
        InstagramStoryViewAdapter adapter = new InstagramStoryViewAdapter(storyList);
        storyViewPager.setAdapter(adapter);
        storyViewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setPivotX(position < 0f ? page.getWidth() : 0f);
                page.setPivotY(page.getHeight() * 0.5f);
                page.setRotationY(90f * position);
            }
        });


        storyViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                userStoryPositionOpened = position;
                super.onPageSelected(position);

            }

        });

        storyViewPager.setCurrentItem(userStoryPositionOpened,false);
        LinearLayout storyHintWrapper = findViewById(R.id.story_hint_wrapper);
        int opened = appDataPref.getInt(Constant.SHOW_HINT_INSTA_STORY,0);
        if(opened>=3)
        {
            storyHintWrapper.setVisibility(View.GONE);
        }
        else
        {
            SharedPreferences.Editor appPrefEditor = appDataPref.edit();
            opened+=1;
            appPrefEditor.putInt(Constant.SHOW_HINT_INSTA_STORY,opened);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    storyHintWrapper.setVisibility(View.GONE);
                }
            },3000);
            appPrefEditor.apply();
        }
    }

    public class InstagramStoryViewAdapter extends RecyclerView.Adapter<InstagramStoryViewAdapter.InstagramStoryViewHolder> {


        List<InstagramStoryModel> stories;

        public InstagramStoryViewAdapter(List<InstagramStoryModel> stories) {
            this.stories = stories;
        }

        @NonNull
        @Override
        public InstagramStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new InstagramStoryViewHolder(LayoutInflater.from(InstagramStoryViewActivity.this).inflate(R.layout.instagram_story_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull InstagramStoryViewHolder holder, int position) {

            Glide.with(InstagramStoryViewActivity.this).load(stories.get(position).getProfileUrl()).into(holder.profileImageView);
            holder.profileUserNameTextView.setText(stories.get(position).getName());

            List<InstagramDownloadModel> modelList = stories.get(position).getStoryList();
            holder.userStoryViewPager.setCurrentItem(itemPositionWithinUserStory,false);
            UserStoriesAdapter adapter = new UserStoriesAdapter(modelList, holder.userStoryViewPager,holder.storyTimeTextView);
            holder.userStoryViewPager.setAdapter(adapter);
            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(holder.userStoryTabLayout,holder.userStoryViewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                }
            });
            tabLayoutMediator.attach();

        }

        @Override
        public int getItemCount() {
            return stories.size();
        }

        public class InstagramStoryViewHolder extends RecyclerView.ViewHolder {

            CircleImageView profileImageView;
            TextView profileUserNameTextView;
            ViewPager2 userStoryViewPager;
            TabLayout userStoryTabLayout;
            TextView storyTimeTextView;

            public InstagramStoryViewHolder(@NonNull View itemView) {
                super(itemView);

                userStoryTabLayout = itemView.findViewById(R.id.user_story_tabLayout);
                profileImageView = itemView.findViewById(R.id.story_item_profile_imageView);
                profileUserNameTextView = itemView.findViewById(R.id.story_item_profile_textView);
                userStoryViewPager = itemView.findViewById(R.id.user_story_viewPager);
                storyTimeTextView = itemView.findViewById(R.id.story_item_time_textView);

            }
        }
    }


    public class UserStoriesAdapter extends RecyclerView.Adapter<UserStoriesAdapter.UserStoriesViewHolder> {
        List<InstagramDownloadModel> userStories;
        ViewPager2 userViewPager;
        TextView storyTimeTextView;

        public UserStoriesAdapter(List<InstagramDownloadModel> userStories, ViewPager2 userViewPager, TextView storyTimeTextView) {
            this.userStories = userStories;
            this.userViewPager = userViewPager;
            this.storyTimeTextView = storyTimeTextView;
        }

        @NonNull
        @Override
        public UserStoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserStoriesViewHolder(LayoutInflater.from(InstagramStoryViewActivity.this).inflate(R.layout.user_story_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull UserStoriesViewHolder holder, int position) {




            Log.i(TAG, "onBindViewHolder: "+userStories.get(position).getTime());

            holder.storyProgressbar.setVisibility(View.VISIBLE);
            userViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int p) {
                    itemPositionWithinUserStory=p;
                    try {
                        storyTimeTextView.setText(getFormattedTime(userStories.get(position).getTime()));
                    }
                    catch (Exception e)
                    {
                        FirebaseLogger.logErrorData("InstagramStoryViewActivity userViewPager.registerOnPageChange ",e.toString());
                        Log.i(TAG, "onPageSelected: "+e);
                    }

                }
            });

            if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {

                holder.storyVideoView.setVisibility(View.VISIBLE);
                holder.storyImageView.setVisibility(View.GONE);
                try {
                    holder.storyVideoView.setVideoPath(userStories.get(position).getUrl());
                    holder.storyVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            holder.storyProgressbar.setVisibility(View.GONE);
                            if(holder.storyVideoView.isShown())
                            {
                                mediaPlayer.start();

                            }
                            if(mediaPlayer.isPlaying())
                            {
                                holder.storyProgressbar.setVisibility(View.GONE);
                            }
                        }
                    });
                    holder.storyVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(holder.storyVideoView.isShown())
                            {
                                if (itemPositionWithinUserStory == userStories.size() - 1) {

                                    if (userStoryPositionOpened < storyList.size() - 1) {
                                        if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {
                                            holder.storyVideoView.stopPlayback();
                                        }
                                        itemPositionWithinUserStory = 0;
                                        storyViewPager.setCurrentItem(userStoryPositionOpened + 1);

                                    } else {
                                        onBackPressed();
                                    }
                                } else {
                                    if (itemPositionWithinUserStory < userStories.size() - 1) {
                                        if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {
                                            holder.storyVideoView.stopPlayback();
                                        }
                                        itemPositionWithinUserStory += 1;
                                        userViewPager.setCurrentItem(itemPositionWithinUserStory);
                                    }
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    FirebaseLogger.logErrorData("InstagramStoryViewActivity holder.storyVideoView.setOnPreparedListener ",e.toString());
                    Log.i(TAG, "onClick: expired");
                }
            } else {
                holder.storyVideoView.setVisibility(View.GONE);
                holder.storyImageView.setVisibility(View.VISIBLE);
                try {
                    Glide.with(InstagramStoryViewActivity.this).load(userStories.get(position).getUrl())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    holder.storyProgressbar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    holder.storyProgressbar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(holder.storyImageView);
                } catch (Exception e) {
                    Log.i(TAG, "onClick: expired");
                    FirebaseLogger.logErrorData("InstagramStoryViewActivity  Glide.with(InstagramStoryViewActivity.this).load(userStories.get(position) ",e.toString());
                }
            }

            holder.preStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    reverseStory(position,holder);


                }
            });

            holder.skipStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    skipStory(position,holder);

                }
            });

            holder.storyDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new DownloadStory().execute(userStories.get(position));
                    showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Starting Download");

                }
            });

            holder.storyWhatsappBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Preparing Whatsapp share");
                    Uri uri = Uri.parse(userStories.get(position).getUrl());
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    whatsappIntent.setType("image/jpeg");
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(whatsappIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        FirebaseLogger.logErrorData("InstagramStoryViewActivity  holder.storyWhatsappBtn.setOnClickListener",ex.toString());
                        showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Whatsapp not installed");
                    }
                }
            });

            holder.storyInstaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Preparing Instagram share");
                    Uri uri = Uri.parse(userStories.get(position).getUrl());
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.instagram.android");
                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    whatsappIntent.setType("image/jpeg");
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(whatsappIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        FirebaseLogger.logErrorData("InstagramStoryViewActivity  holder.storyInstaBtn.setOnClickListener",ex.toString());
                        showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Instagram not installed");
                    }
                }
            });

            holder.storyShareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    showActionResult(holder.storyInfoTextView,holder.storyInfoWrapper,"Preparing for share");
                    Uri uri = Uri.parse(userStories.get(position).getUrl());
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
//                    intent.putExtra(Intent.EXTRA_TEXT, "Shared with InstaSave. Get your now http://bit.ly/3j8CQFI");
                    intent.putExtra(Intent.EXTRA_TEXT, "Shared with InstaSave");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if(userStories.get(position).getType().equals(Constant.GRAPH_VIDEO))
                    {
                        intent.setType("video/*");
                    }
                    else
                    {
                        intent.setType("image/*");
                    }
                    startActivity(Intent.createChooser(intent, "Sharing via"));
                }
            });


        }

        private String getFormattedTime(long time) {
            long secondsRemaining = (System.currentTimeMillis()/1000)-time;
            if (secondsRemaining>(60*60))
            {
                long hrs = secondsRemaining/(60*60);
                return hrs+" h";
            }
            else if (secondsRemaining>(60))
            {
                long mins = secondsRemaining/(60);
                return mins+" m";
            }
            else if(secondsRemaining>0)
            {
                return secondsRemaining+" s";
            }
            return "a while ago";

        }

        private void showActionResult(TextView statusInfoTextView, RelativeLayout statusActionWrapper, String result) {
            statusInfoTextView.setText(result);
            TransitionManager.beginDelayedTransition(statusActionWrapper,new AutoTransition());
            statusInfoTextView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(statusInfoTextView!=null &&statusActionWrapper!=null)
                    {
                        TransitionManager.beginDelayedTransition(statusActionWrapper,new AutoTransition());
                        statusInfoTextView.setVisibility(View.GONE);
                    }

                }
            },1500);

        }

        public void reverseStory(int position, UserStoriesViewHolder holder)
        {
            if (itemPositionWithinUserStory == 0) {
                if (userStoryPositionOpened > 0 ) {

                    if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO) && holder.storyVideoView.getCurrentPosition()>5000) {
                        holder.storyVideoView.seekTo(0);
                    }
                    else
                    {
                        if(userStories.get(position).getType().equals(Constant.GRAPH_VIDEO))
                        {
                            holder.storyVideoView.stopPlayback();
                        }

                        itemPositionWithinUserStory = storyList.get(userStoryPositionOpened - 1).getStoryList().size() - 1;
                        storyViewPager.setCurrentItem(userStoryPositionOpened - 1);
                    }

                } else if(userStoryPositionOpened==0 && itemPositionWithinUserStory==0) {

                    if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO) && holder.storyVideoView.getCurrentPosition()>5000) {
                        holder.storyVideoView.seekTo(0);
                    }
                    else
                    {
                        Toast.makeText(InstagramStoryViewActivity.this, "First story", Toast.LENGTH_SHORT).show();
                    }

                }
            } else {
                if (itemPositionWithinUserStory > 0) {


                    if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO) && holder.storyVideoView.getCurrentPosition()>5000) {
                        holder.storyVideoView.seekTo(0);
                    }
                    else {
                        if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {
                            holder.storyVideoView.stopPlayback();
                        }
                        itemPositionWithinUserStory -= 1;
                        userViewPager.setCurrentItem(itemPositionWithinUserStory);

                    }


                }
            }
        }

        public void skipStory(int position, UserStoriesViewHolder holder)
        {
            if (itemPositionWithinUserStory == userStories.size() - 1) {

                if (userStoryPositionOpened < storyList.size() - 1) {
                    if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {
                        holder.storyVideoView.stopPlayback();
                    }
                    itemPositionWithinUserStory = 0;
                    storyViewPager.setCurrentItem(userStoryPositionOpened + 1);

                } else {
                    Toast.makeText(InstagramStoryViewActivity.this, "Last story", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (itemPositionWithinUserStory < userStories.size() - 1) {
                    if (userStories.get(position).getType().equals(Constant.GRAPH_VIDEO)) {
                        holder.storyVideoView.stopPlayback();
                    }
                    itemPositionWithinUserStory += 1;
                    userViewPager.setCurrentItem(itemPositionWithinUserStory);
                }
            }
        }

        @Override
        public int getItemCount() {
            return userStories.size();
        }

        public class UserStoriesViewHolder extends RecyclerView.ViewHolder {

            ImageView storyImageView;
            VideoView storyVideoView;
            ProgressBar storyProgressbar;
            View skipStory, preStory;
            ImageButton storyDownloadBtn, storyInstaBtn, storyWhatsappBtn, storyShareBtn;
            TextView storyInfoTextView;
            RelativeLayout storyInfoWrapper;

            public UserStoriesViewHolder(@NonNull View itemView) {
                super(itemView);

                skipStory = itemView.findViewById(R.id.story_item_skip);
                preStory = itemView.findViewById(R.id.story_item_reverse);
                storyProgressbar = itemView.findViewById(R.id.story_item_progressbar);
                storyImageView = itemView.findViewById(R.id.story_item_imageView);
                storyVideoView = itemView.findViewById(R.id.story_item_videoView);

                storyInfoTextView = itemView.findViewById(R.id.story_info_textView);
                storyInfoWrapper = itemView.findViewById(R.id.story_btn_wrapper);


                storyDownloadBtn = itemView.findViewById(R.id.story_download_btn);
                storyInstaBtn = itemView.findViewById(R.id.story_instagram_btn);
                storyWhatsappBtn = itemView.findViewById(R.id.story_whatsapp_btn);
                storyShareBtn = itemView.findViewById(R.id.story_share_btn);

                storyVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (storyVideoView.isShown()) {
                            mp.start();
                        }
                        storyProgressbar.setVisibility(View.GONE);
                    }
                });
                storyVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (storyVideoView.isShown()) {
                            mp.start();
                        }
                    }
                });


            }
        }
    }

    public class DownloadStory extends AsyncTask<InstagramDownloadModel, Void, Void>
    {

        @Override
        protected Void doInBackground(InstagramDownloadModel... downloadModels) {
            LinkDatabase linkDatabase = Room.databaseBuilder(InstagramStoryViewActivity.this, LinkDatabase.class, Constant.LINK_DB).fallbackToDestructiveMigration().build();
            if(!linkDatabase.getLinkDao().checkInstaLinkExists(downloadModels[0].getUrl()))
            {
                linkDatabase.getLinkDao().addLink(new InstaLink(downloadModels[0].getUrl(), 0,false,true,false,downloadModels[0].getType()));
                if(new Util(InstagramStoryViewActivity.this).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.ENQUEUED && new Util(InstagramStoryViewActivity.this).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.RUNNING)
                {
                    OneTimeWorkRequest downloadWork  = new OneTimeWorkRequest.Builder(InstaDownloadWorker.class).build();
                    WorkManager.getInstance(InstagramStoryViewActivity.this).beginUniqueWork(Constant.INSTA_DOWNLOAD_WORKER_TAG, ExistingWorkPolicy.KEEP,downloadWork).enqueue();
                }
            }
            return null;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}