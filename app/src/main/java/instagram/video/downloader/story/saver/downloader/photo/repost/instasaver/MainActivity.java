package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity.InstagramLoginActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity.SettingsActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Activity.StatusViewActivity;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.AsyncTask.DownloadIGPost;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.InstaLink;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Db.LinkDatabase;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramDownloadModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.InstagramStoryModel;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Model.Status;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Constant;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.FirebaseLogger;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.Util;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils.WrapGridLayoutManager;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.ViewHolder.StatusViewHolder;
import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Worker.InstaDownloadWorker;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLogs";
    boolean doubleBackToExitPressedOnce = false;

    Toolbar mainToolbar;
    List<Status> instaPostList;
    RecyclerView instaRecyclerView;

    View instaSelectedView;
    ImageButton instaSelectedViewBackBtn,instaSelectedViewMoreBtn;

    InstaAdapter instaAdapter;
    PopupMenu popup;
    TextView noStatusSavedTextView;
    ConstraintLayout rootInstaLayout;
    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView storiesRecyclerView;
    TextView storiesInfoTextView;
    ProgressBar storiesProgressbar;
    CardView storiesRecyclerViewWrapper;
    NestedScrollView scrollView;

    Parcelable state = null;
    ExecutorService executor;
    Handler handler;

    static MainActivity mainActivity;
    List<InstagramStoryModel> userStoriesList;
    static final int WRITE_PERMISSION_REQ_CODE = 100;
    public static FileObserver observer;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }
    Snackbar downloadSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        String theme = appDataPref.getString(Constant.THEME_KEY, Constant.THEME_VAL_SYSTEM);
        switch (theme) {
            case Constant.THEME_VAL_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case Constant.THEME_VAL_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Constant.THEME_VAL_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        rootInstaLayout = findViewById(R.id.root_insta_layout);
        instaSelectedView = findViewById(R.id.doc_selected_view);
        instaSelectedViewBackBtn = instaSelectedView.findViewById(R.id.doc_selected_back_btn);
        instaSelectedViewMoreBtn = instaSelectedView.findViewById(R.id.doc_selected_more_button);
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        try {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        } catch (Exception e) {
            FirebaseLogger.logErrorData("MainActivity getSupportActionbar ",e.toString());
        }
        scrollView = findViewById(R.id.insta_frag_scrollView);
        storiesRecyclerViewWrapper = findViewById(R.id.stories_recyclerView_wrapper);
        storiesInfoTextView = findViewById(R.id.stories_info_textView);
        storiesProgressbar = findViewById(R.id.stories_progressbar);
        storiesRecyclerView = findViewById(R.id.stories_recyclerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));

        swipeRefreshLayout = findViewById(R.id.insta_frag_swipe_refresh_layout);
        noStatusSavedTextView = findViewById(R.id.insta_no_status_text_view);
        instaRecyclerView = findViewById(R.id.insta_fragment_recyclerView);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            instaRecyclerView.setLayoutManager(new WrapGridLayoutManager(MainActivity.this, 4));
        } else {
            instaRecyclerView.setLayoutManager(new WrapGridLayoutManager(MainActivity.this, 3));
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                if (!TextUtils.isEmpty(appDataPref.getString(Constant.COOKIE, ""))) {
                    FetchInstagramStories();
                } else {
                    storiesInfoTextView.setText("Tap here to see your stories anonymously");
                    storiesProgressbar.setVisibility(View.GONE);
                    storiesRecyclerViewWrapper.setVisibility(View.GONE);
                    storiesInfoTextView.setVisibility(View.VISIBLE);
                }
                loadSavedInstagramContents();
            }
        });


        if (!TextUtils.isEmpty(appDataPref.getString(Constant.COOKIE, ""))) {
            FetchInstagramStories();
        } else {
            storiesProgressbar.setVisibility(View.GONE);
            storiesRecyclerViewWrapper.setVisibility(View.GONE);
            storiesInfoTextView.setVisibility(View.VISIBLE);
        }

        loadSavedInstagramContents();

        storiesInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, InstagramLoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();

            }
        });

        instaSelectedViewBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(instaAdapter!=null)
                {
                    instaAdapter.clearSelection();
                }

            }
        });

        instaSelectedViewMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(MainActivity.this, instaSelectedViewMoreBtn);
                popup.getMenuInflater().inflate(R.menu.pop_up_saved_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.pop_clear_all:
                                if(instaAdapter!=null)
                                {
                                    instaAdapter.clearSelection();
                                }
                                break;
                            case R.id.pop_select_all:
                                if(instaAdapter!=null)
                                {
                                    for(int i=0;i<instaPostList.size();i++)
                                    {
                                        instaPostList.get(i).setSelected(true);
                                    }
                                }
                                TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                                docSelectedCountTextView.setText("Selected: " + instaPostList.size());
                                instaAdapter.notifyDataSetChanged();
                                break;

                            case R.id.pop_delete:
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for(int i=0;i<instaPostList.size();i++)
                                        {
                                            if(instaPostList.get(i).isSelected())
                                            {
                                                File file = new File(instaPostList.get(i).getFilePath());
                                                if(!instaPostList.get(i).isVideo())
                                                {
                                                    String[] projection = {MediaStore.Images.Media._ID};
                                                    String selection = MediaStore.Images.Media.DATA + " = ?";
                                                    String[] selectionArgs = new String[]{file.getAbsolutePath()};
                                                    // Query for the ID of the media matching the file path
                                                    Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                                    ContentResolver contentResolver = getContentResolver();
                                                    Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                                                    if (c.moveToFirst()) {
                                                        long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                                                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                                        contentResolver.delete(deleteUri, null, null);
                                                    }
                                                }
                                                else
                                                {
                                                    String[] projection = {MediaStore.Video.Media._ID};
                                                    String selection = MediaStore.Video.Media.DATA + " = ?";
                                                    String[] selectionArgs = new String[]{file.getAbsolutePath()};
                                                    Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                                                    ContentResolver contentResolver = getContentResolver();
                                                    Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                                                    if (c.moveToFirst()) {
                                                        long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                                                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                                                        contentResolver.delete(deleteUri, null, null);
                                                    }
                                                }
                                            }

                                        }

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                if(instaAdapter!=null)
                                                {
                                                    instaAdapter.clearSelection();
                                                }
                                                loadSavedInstagramContents();

                                            }
                                        });

                                    }
                                });
                                break;
                            case R.id.pop_share:
                                ArrayList<Uri> uris = new ArrayList<>();
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
//                                intent.putExtra(Intent.EXTRA_TEXT, "Shared with Swift. Get your now http://bit.ly/3j8CQFI");
                                intent.putExtra(Intent.EXTRA_TEXT, "Shared with InstaSave");
                                intent.setType("*/*");
                                for (int i = 0; i < instaPostList.size(); i++) {
                                    if (instaPostList.get(i).isSelected()) {
                                        Uri uri = FileProvider.getUriForFile(MainActivity.this, "instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.fileprovider", new File(instaPostList.get(i).getFilePath()));
                                        uris.add(uri);
                                    }
                                }
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                startActivity(Intent.createChooser(intent, "Sharing via"));
                                showSnackBarMessage("Preparing for share");

                                break;

                        }
                        popup.dismiss();
                        return true;
                    }
                });
                popup.show();
            }
        });

        downloadSnackbar = Snackbar.make(findViewById(R.id.root_insta_layout),"Download in progress", BaseTransientBottomBar.LENGTH_INDEFINITE);
        IntentFilter filter = new IntentFilter("com.refresh.screen");
        this.registerReceiver(new ScreenRefreshReceiver(), filter);

    }

    public class ScreenRefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getExtras().getString(Constant.BROADCAST_ACTION);
            if (action.equals(Constant.DOWNLOAD_STARTED))
            {
                if(!downloadSnackbar.isShown())
                {
                    downloadSnackbar.show();
                }
            }
            else if(action.equals(Constant.DOWNLOAD_COMPLETE))
            {
                downloadSnackbar.dismiss();
                loadSavedInstagramContents();
            }
            else if(action.equals(Constant.INSTA_STORY_UPDATED))
            {
                FetchInstagramStories();
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        InstaSave.activityResumed();
    }



    public void scrollToPosition()
    {
        if(state!=null && instaRecyclerView!=null)
        {
            instaRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && instaRecyclerView != null) {

            setRecyclerviewPosition();
            instaRecyclerView.setLayoutManager(new WrapGridLayoutManager(this, 3));
            scrollToPosition();


        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && instaRecyclerView != null) {

            setRecyclerviewPosition();
           instaRecyclerView.setLayoutManager(new WrapGridLayoutManager(this, 4));
            scrollToPosition();


        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        Log.i(TAG, "onWindowFocusChanged: ");
        if(hasFocus)
        {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            try {
                if(clipboard.hasPrimaryClip())
                {
                    ClipData clipData = clipboard.getPrimaryClip();
                    String url = clipData.getItemAt(0).getText().toString().trim();
                    if (!getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE).getString(Constant.COPIED_URL, Constant.COPIED_URL).equals(url.replace("https://www.instagram.com/", "")) && url.contains("instagram.com")) {

                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.exit_dialog_layout);
                        dialog.setCanceledOnTouchOutside(false);

                        Button yesBtn = (Button) dialog.findViewById(R.id.dialog_yes_button);
                        Button noBtn = (Button) dialog.findViewById(R.id.dialog_no_button);
                        TextView headerTextView =  dialog.findViewById(R.id.dialog_header_textView);
                        TextView messageTextView =  dialog.findViewById(R.id.dialog_message_textView);

                        headerTextView.setText("IG Url Detected");
                        messageTextView.setText("Download content from "+url);

                        yesBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();
                                try {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    if(clipboard.hasPrimaryClip())
                                    {
                                        String textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim();
                                        if (clipboard.getPrimaryClip().getItemCount() > 0 && !TextUtils.isEmpty(textToPaste) && textToPaste.contains("instagram.com/"))
                                        {
                                            ClipData clipData = ClipData.newPlainText("", "");
                                            clipboard.setPrimaryClip(clipData);
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
//                                    Log.i(TAG, "onPostExecute: "+e);

                                }
                                askToDownloadInstaPost(url);
                            }
                        });

                        noBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog.dismiss();

                                try {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    if(clipboard.hasPrimaryClip())
                                    {
                                        String textToPaste = clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim();
                                        if (clipboard.getPrimaryClip().getItemCount() > 0 && !TextUtils.isEmpty(textToPaste) && textToPaste.contains("instagram.com/"))
                                        {
                                            ClipData clipData = ClipData.newPlainText("", "");
                                            clipboard.setPrimaryClip(clipData);
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
//                                    Log.i(TAG, "onPostExecute: "+e);
                                }

                            }
                        });
                        if(!dialog.isShowing())
                        {
                            dialog.show();
                        }
//                    Log.i(TAG, "onWindowFocusChanged: "+url);
                    }
                }

            } catch (Exception e) {
//                Log.i(TAG, "onWindowFocusChanged: " + e);
                FirebaseLogger.logErrorData("MainActivity onWindowFocusChanged ",e.toString());
            }
        }
    }


    public void FetchInstagramStories()
    {
//        Log.i(TAG, "FetchInstagramStories: func called");
        storiesProgressbar.setVisibility(View.VISIBLE);
        storiesInfoTextView.setText("Fetching your stories");
        storiesInfoTextView.setVisibility(View.VISIBLE);
        userStoriesList = new ArrayList<>();

        executor.execute(new Runnable() {
            @Override
            public void run() {

                String data = "";
                try {

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
                    {
                        String fileName = "story.txt";
                        File outputFile = new File(getFilesDir()+fileName);
                        FileInputStream fis = new FileInputStream(outputFile);
                        DataInputStream in = new DataInputStream(fis);
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(in));
                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            data = data + strLine;
                        }
                        in.close();
                    }
                    else
                    {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("story.txt")));
                        StringBuilder builder = new StringBuilder();
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            builder.append("\n").append(line);
                        }
                        reader.close();
                        data = builder.toString();
                    }
//                    Log.i(TAG, "FetchInstagramStories: data "+data);
                    if(!TextUtils.isEmpty(data))
                    {
                        JSONObject stories = new JSONObject(data);
                        JSONArray storiesList = stories.getJSONArray("stories");
                        for(int i=0;i<storiesList.length();i++)
                        {   List<InstagramDownloadModel> storyList = new ArrayList<>();
                            String name = storiesList.getJSONObject(i).getString("name");
                            String dp = storiesList.getJSONObject(i).getString("dp");
                            String uid = storiesList.getJSONObject(i).getString("uid");
                            JSONArray userStoryList = storiesList.getJSONObject(i).getJSONArray("stories");
                            for(int j=0;j<userStoryList.length();j++)
                            {
                                String type = userStoryList.getJSONObject(j).getString("type");
                                String url = userStoryList.getJSONObject(j).getString("url");
                                long time = userStoryList.getJSONObject(j).getLong("expire_at");
                                if(time<(System.currentTimeMillis()/1000))
                                {
                                    storyList.add(new InstagramDownloadModel(type,url,time));
                                }
                            }
                            userStoriesList.add(new InstagramStoryModel(name,dp,uid,storyList));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    storiesProgressbar.setVisibility(View.GONE);
                                    if (userStoriesList.size() > 0) {
                                        storiesInfoTextView.setVisibility(View.GONE);
                                        storiesRecyclerViewWrapper.setVisibility(View.VISIBLE);
                                        InstaStoriesAdapter adapter = new InstaStoriesAdapter(userStoriesList);
                                        storiesRecyclerView.setAdapter(adapter);
                                    } else {
                                        storiesRecyclerView.setAdapter(null);
                                        storiesInfoTextView.setText("No stories to watch");
                                        storiesRecyclerViewWrapper.setVisibility(View.GONE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                storiesInfoTextView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out));
                                                storiesInfoTextView.getAnimation().start();
                                                storiesInfoTextView.setVisibility(View.GONE);

                                            }
                                        }, 2500);
                                    }

                                }
                            });
                        }
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
//                    Log.i(TAG, "FetchInstagramStories : "+e);
                    FirebaseLogger.logErrorData("MainActivity FetchInstagramStories exception ",e.toString());
                }

            }


        });
    }


    public void askToDownloadInstaPost(String textToPaste) {

        if (textToPaste.startsWith("https://instagram.com/")) {
            textToPaste = textToPaste.replace("https://instagram.com/", "https://www.instagram.com/");
        }
        if (textToPaste.contains("instagram.com")) {
            new DownloadIGPost(MainActivity.this).execute(textToPaste);
        }
    }


    public void loadSavedInstagramContents() {

//        Log.i(TAG, "loadSavedStatuses: ");
        instaPostList = new ArrayList<>();
        instaAdapter = new InstaAdapter(instaPostList);
        instaRecyclerView.setAdapter(instaAdapter);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                getDownloadedInstaContent();
            }
            else
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQ_CODE);

            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

//                https://www.instagram.com/p/CPKlT9ppChK/?utm_source=ig_web_copy_link
                getDownloadedInstaContent();
            }
            else
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQ_CODE);

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case WRITE_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSavedInstagramContents();
                } else if(grantResults.length > 0){
                    Toast.makeText(MainActivity.this, "Permissions required", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void getDownloadedInstaContent() {

//        Log.i(TAG, "getInstaPostsApi29AndAbove: inside function");

        instaPostList.clear();

        executor.execute(new Runnable() {
            @Override
            public void run() {

                String[] projection = new String[]{
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media.DATA
                };
                String sortOrder = MediaStore.Video.Media.DATE_ADDED + " ASC";
                String selectionArgs = "Instagram_content_%";

                try
                {
                    Cursor cursor = getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            MediaStore.Video.Media.DISPLAY_NAME + " LIKE ?",
                            new String[] {selectionArgs},
                            sortOrder);
                    // Cache column indices.
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int nameColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                    int data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);
                        String dateAdded = cursor.getString(dateAddedColumn);
                        String filePath = cursor.getString(data);
                        int size = cursor.getInt(sizeColumn);
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                        long addedTime = System.currentTimeMillis();
                        try {
                            addedTime = Long.parseLong(dateAdded);
//                            Log.i(TAG, "run: "+addedTime);
                        }
                        catch (Exception e)
                        {
                            FirebaseLogger.logErrorData("MainActivity getInstaPostsApi29AndAbove video data timeparseing",e.toString());
//                            Log.i(TAG, "run: "+e);
                        }

//                        Log.i(TAG, "loadInstaPosts: "+ contentUri+" "+ name+" "+ size+" "+dateAdded+" "+filePath);
                        instaPostList.add(new Status(filePath,addedTime,false,name.contains(".mp4")));

                    }

                    Cursor cursor_image = getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            MediaStore.Video.Media.DISPLAY_NAME + " LIKE ?",
                            new String[] {selectionArgs},
                            sortOrder);
                    while (cursor_image.moveToNext()) {
                        long id = cursor_image.getLong(idColumn);
                        String name = cursor_image.getString(nameColumn);
                        String dateAdded = cursor_image.getString(dateAddedColumn);
                        String filePath = cursor_image.getString(data);
                        int size = cursor_image.getInt(sizeColumn);
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        long addedTime = System.currentTimeMillis();
                        try {
                            addedTime = Long.parseLong(dateAdded);
//                            Log.i(TAG, "run: "+addedTime);
                        }
                        catch (Exception e)
                        {
                            FirebaseLogger.logErrorData("MainActivity getInstaPostsApi29AndAbove image data timeparsing",e.toString());
//                            Log.i(TAG, "run: "+e);
                        }

                        instaPostList.add(new Status(filePath,addedTime,false,name.contains(".mp4")));


                    }
                    Collections.sort(instaPostList);
                }
                catch (Exception e)
                {
//                    Log.i(TAG, "getInstaPostsApi29AndAbove: "+e);
                    FirebaseLogger.logErrorData("MainActivity getDownloadedInstaContent ",e.toString());
                }


                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefreshLayout.setRefreshing(false);
                        if (instaPostList.size() > 0) {
                            noStatusSavedTextView.setVisibility(View.GONE);
                        } else {
                            noStatusSavedTextView.setVisibility(View.VISIBLE);
                            TransitionManager.beginDelayedTransition(rootInstaLayout, new AutoTransition());
                        }
//                        instaRecyclerView.removeAllViews();
                        instaAdapter.notifyDataSetChanged();
                        if (state != null) {
                            instaRecyclerView.getLayoutManager().onRestoreInstanceState(state);
                        }

                    }
                });

            }
        });

    }

    private boolean alreadyContains(List<Status> instaPostList, String filePath) {

        for(int i=0;i<instaPostList.size();i++)
        {
            if(instaPostList.get(i).getFilePath().equals(filePath))
            {
                return true;
            }
        }
        return false;

    }


    public class InstaStoriesAdapter extends RecyclerView.Adapter<InstaStoriesAdapter.InstaStoryViewHolder> {
        List<InstagramStoryModel> storyModelList = new ArrayList<>();
        AnimationDrawable anim;
        public InstaStoriesAdapter(List<InstagramStoryModel> storyModelList) {
            this.storyModelList = storyModelList;
        }

        @NonNull
        @Override
        public InstaStoriesAdapter.InstaStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new InstaStoryViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.instagram_story_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull InstaStoriesAdapter.InstaStoryViewHolder holder, int position) {

            holder.storyProgressbar.setVisibility(View.VISIBLE);





            Glide.with(MainActivity.this).load(storyModelList.get(position).getProfileUrl())
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
                    .into(holder.profileImageView);
            holder.nameTextView.setText(storyModelList.get(position).getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(storyModelList.get(position).getStoryList().size()>0)
                    {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                LinkDatabase linkDatabase = Room.databaseBuilder(MainActivity.this, LinkDatabase.class, Constant.LINK_DB).fallbackToDestructiveMigration().build();
                                List<InstagramDownloadModel> modelList = storyModelList.get(position).getStoryList();
                                for(int i=0;i<modelList.size();i++)
                                {
                                    if(!linkDatabase.getLinkDao().checkInstaLinkExists(modelList.get(i).getUrl()))
                                    {
                                        linkDatabase.getLinkDao().addLink(new InstaLink(modelList.get(i).getUrl(), 0,false,true,false,modelList.get(i).getType()));
                                    }
                                }
                                if(new Util(MainActivity.this).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.ENQUEUED && new Util(MainActivity.this).getStateOfWork(Constant.INSTA_DOWNLOAD_WORKER_TAG) != WorkInfo.State.RUNNING)
                                {
                                    OneTimeWorkRequest downloadWork  = new OneTimeWorkRequest.Builder(InstaDownloadWorker.class).build();
                                    WorkManager.getInstance(MainActivity.this).beginUniqueWork(Constant.INSTA_DOWNLOAD_WORKER_TAG, ExistingWorkPolicy.KEEP,downloadWork).enqueue();
                                }
                            }
                        });
                    }
                    else
                    {
                        String reqUrl = "https://i.instagram.com/api/v1/feed/user/"+storyModelList.get(position).getUid()+"/reel_media/";
                        new DownloadIGPost(MainActivity.this).execute(reqUrl);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return storyModelList.size();
        }

        public class InstaStoryViewHolder extends RecyclerView.ViewHolder {

            TextView nameTextView;
            CircleImageView profileImageView;
            ProgressBar storyProgressbar;
            LinearLayout instaBackground;

            public InstaStoryViewHolder(@NonNull View itemView) {
                super(itemView);

                instaBackground = itemView.findViewById(R.id.insta_user_background);
                nameTextView = itemView.findViewById(R.id.story_layout_textView);
                profileImageView = itemView.findViewById(R.id.story_layout_imageView);
                storyProgressbar = itemView.findViewById(R.id.story_layout_progressbar);
                AnimationDrawable anim = (AnimationDrawable) instaBackground.getBackground();
                anim.setEnterFadeDuration(1500);
                anim.setExitFadeDuration(1500);
                if (anim != null && !anim.isRunning())
                    anim.start();
            }
        }
    }

    public class InstaAdapter extends RecyclerView.Adapter<StatusViewHolder> {

        private static final String TAG = "SavedStatusAdapterLog";
        List<Status> postList;
        boolean isMultipleSelectionEnabled;
        Dialog longClickDialog;
        Rect outRect;
        int[] location;
        Animation scaleDownAnimation;
        boolean isDownloadBtnScaledUp, isShareBtnScaledUp;

        public InstaAdapter(List<Status> pdfList) {
            this.postList = pdfList;
            outRect = new Rect();
            location = new int[2];
            isMultipleSelectionEnabled = false;
            isDownloadBtnScaledUp = false;
            isShareBtnScaledUp = false;
            longClickDialog = new Dialog(MainActivity.this, android.R.style.ThemeOverlay_Material_Dialog);
            scaleDownAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_down);
        }


        @NonNull
        @Override
        public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StatusViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.photo_layout, parent, false));


        }

        @Override
        public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {

            if (postList.get(position).isVideo()) {
                holder.statusIsVideoImageButton.setVisibility(View.VISIBLE);
            } else {
                holder.statusIsVideoImageButton.setVisibility(View.GONE);

            }

            if (postList.get(position).isSelected()) {
                holder.statusSelectedLayout.setVisibility(View.VISIBLE);
            } else {
                holder.statusSelectedLayout.setVisibility(View.GONE);
            }


            holder.statusProgressbar.setVisibility(View.VISIBLE);
            holder.statusImageView.setVisibility(View.VISIBLE);
            Glide.with(MainActivity.this).load(postList.get(position).getFilePath())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.statusProgressbar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.statusProgressbar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.statusImageView);

            holder.statusSelectedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isMultipleSelectionEnabled) {
                        postList.get(position).setSelected(false);
                        holder.statusSelectedLayout.setVisibility(View.GONE);
                        if (!checkMultiSelectionDocsExist()) {
                            circleReveal(instaSelectedView, 1, true, false);
                            isMultipleSelectionEnabled = false;
                        }
                        holder.itemView.startAnimation(scaleDownAnimation);

                        TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                        docSelectedCountTextView.setText("Selected: " + getSelectedPostCount());
                    }

                }
            });


            holder.statusSelectedLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMultipleSelectionEnabled) {
                        postList.get(position).setSelected(false);
                        holder.statusSelectedLayout.setVisibility(View.GONE);
                        if (!checkMultiSelectionDocsExist()) {
                            circleReveal(instaSelectedView, 1, true, false);
                            isMultipleSelectionEnabled = false;
                        }
                        holder.itemView.startAnimation(scaleDownAnimation);
                        TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                        docSelectedCountTextView.setText("Selected: " + getSelectedPostCount());
                    }
                }
            });



            holder.statusIsVideoImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!isMultipleSelectionEnabled) {
                        Intent i = new Intent(MainActivity.this, StatusViewActivity.class);
                        i.putParcelableArrayListExtra(Constant.STATUS_PASS_KEY, (ArrayList<? extends Parcelable>) postList);
                        i.putExtra(Constant.STATUS_PASS_POSITION, position);
                        i.putExtra(Constant.VIEWING_SAVED_STATUS, true);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } else {
                        postList.get(position).setSelected(true);
                        holder.statusSelectedLayout.setVisibility(View.VISIBLE);
                        TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                        docSelectedCountTextView.setText("Selected: " + getSelectedPostCount());
                    }
                    holder.itemView.startAnimation(scaleDownAnimation);

                }
            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {

                GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (!isMultipleSelectionEnabled) {
                            showDialog(position);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (longClickDialog != null && !longClickDialog.isShowing()) {
                            if (!isMultipleSelectionEnabled) {
                                holder.statusSelectedLayout.setVisibility(View.VISIBLE);
                                postList.get(position).setSelected(true);
                                isMultipleSelectionEnabled = true;
                                holder.itemView.startAnimation(scaleDownAnimation);
                                circleReveal(instaSelectedView, 1, true, true);
                                TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                                docSelectedCountTextView.setText("Selected: " + getSelectedPostCount());

                            }

                        }
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (isMultipleSelectionEnabled) {
                            if (postList.get(position).isSelected()) {
                                postList.get(position).setSelected(false);
                                holder.statusSelectedLayout.setVisibility(View.GONE);
                                if (!checkMultiSelectionDocsExist()) {
                                    circleReveal(instaSelectedView, 1, true, false);
                                    isMultipleSelectionEnabled = false;

                                }

                            } else {
                                postList.get(position).setSelected(true);
                                holder.statusSelectedLayout.setVisibility(View.VISIBLE);
                            }
                            TextView docSelectedCountTextView = instaSelectedView.findViewById(R.id.doc_selected_count_textView);
                            docSelectedCountTextView.setText("Selected: " + getSelectedPostCount());
                            holder.itemView.startAnimation(scaleDownAnimation);
                        }
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (!isMultipleSelectionEnabled) {
                            Intent i = new Intent(MainActivity.this, StatusViewActivity.class);
                            i.putParcelableArrayListExtra(Constant.STATUS_PASS_KEY, (ArrayList<? extends Parcelable>) postList);
                            i.putExtra(Constant.STATUS_PASS_POSITION, position);
                            i.putExtra(Constant.VIEWING_SAVED_STATUS, true);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            holder.itemView.startAnimation(scaleDownAnimation);
                        }
                        return true;
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }

            });

        }

        private boolean checkMultiSelectionDocsExist() {
            for (Status model : postList) {
                if (model.isSelected()) {
                    return true;
                }
            }
            return false;
        }


        public void circleReveal(View viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {
            final View myView = viewID;

            int width = myView.getWidth();
//
//        if(posFromRight>0)
//            width=width-(getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)/ 2);
            if (containsOverflow)
                width -= (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) / 2);

            int cx = width;
            int cy = myView.getHeight() / 2;

            Animator anim;
            if (isShow)
                anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
            else {
                anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);
            }

            anim.setDuration((long) 220);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isShow) {
                        super.onAnimationEnd(animation);
                        myView.setVisibility(View.GONE);
                        try {
                            mainToolbar.setVisibility(View.VISIBLE);
                        }
                        catch (Exception e)
                        {
                            FirebaseLogger.logErrorData("MainActivity circleReveal setting toolbar visibility",e.toString());
//                            Log.i(TAG, "onAnimationEnd: "+e);
                        }
                    }
                    else
                    {
                        try {
                            mainToolbar.setVisibility(View.INVISIBLE);
                        }
                        catch (Exception e)
                        {
                            FirebaseLogger.logErrorData("MainActivity circleReveal hiding toolbar visibility",e.toString());
//                            Log.i(TAG, "onAnimationEnd: "+e);
                        }
                    }
                }
            });

            // make the view visible and start the animation
            if (isShow)
                myView.setVisibility(View.VISIBLE);

            // start the animation
            anim.start();


        }

        public List<Status> getSelectedPosts() {
            List<Status> myStatus = new ArrayList<>();
            for (int i = 0; i < postList.size(); i++) {
                if (postList.get(i).isSelected()) {
                    myStatus.add(postList.get(i));
                }
            }
            return myStatus;
        }

        public int getSelectedPostCount() {
            int count = 0;
            for (int i = 0; i < postList.size(); i++) {
                if (postList.get(i).isSelected()) {
                    count++;
                }
            }
//            Log.i(TAG, "getSelectedDocCount: " + count);
            return count;
        }

        public void clearSelection() {
            try {
                for (int i = 0; i < postList.size(); i++) {
                    postList.get(i).setSelected(false);
                    notifyItemChanged(i);
                }
                circleReveal(instaSelectedView, 1, true, false);
//            docSelectedView.get().setVisibility(View.GONE);

            } catch (Exception e) {
//                Log.i(TAG, "clearSelection: " + e);
                FirebaseLogger.logErrorData("MainActivity clearSelection ",e.toString());
            }
            if (popup != null) {
                popup.dismiss();
                popup.dismiss();
            }
            isMultipleSelectionEnabled = false;
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(findViewById(R.id.root_insta_layout), message, BaseTransientBottomBar.LENGTH_LONG).show();
    }



    @Override
    public void onPause() {
        super.onPause();
        setRecyclerviewPosition();
        InstaSave.activityPaused();
    }

    public void setRecyclerviewPosition() {
        state = instaRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onBackPressed() {

        long currentTimeInMillis = System.currentTimeMillis()-(60*1000);
        SharedPreferences appDataPref = getSharedPreferences(Constant.THEME_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor appDataPrefEditor = appDataPref.edit();
        int downloadedCount = appDataPref.getInt(Constant.DOWNLOADED_COUNT,0);
        boolean isRated = appDataPref.getBoolean(Constant.APP_RATED,false);
        long isRatingTime = appDataPref.getLong(Constant.APP_RATING_TIME,currentTimeInMillis);

//        Log.i(TAG, "onBackPressed: isRated "+isRated);
//        Log.i(TAG, "onBackPressed: isRatingTime "+isRatingTime);
//        Log.i(TAG, "onBackPressed: downloadedCount "+downloadedCount);

        if (instaAdapter != null && instaAdapter.getSelectedPostCount() > 0) {
            instaAdapter.clearSelection();
        }
        else if(downloadedCount>0 && !isRated && System.currentTimeMillis()>isRatingTime)
        {
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.exit_dialog_layout);
            dialog.setCanceledOnTouchOutside(false);

            Button yesBtn = (Button) dialog.findViewById(R.id.dialog_yes_button);
            Button noBtn = (Button) dialog.findViewById(R.id.dialog_no_button);
            TextView headerTextView =  dialog.findViewById(R.id.dialog_header_textView);
            TextView messageTextView =  dialog.findViewById(R.id.dialog_message_textView);

            headerTextView.setText("Rate InstaSave");
            messageTextView.setText("We have helped you with "+downloadedCount+" download(s). If you enjoyed it, please rate us 5 stars!!! \n\uD83C\uDF1F \uD83D\uDE07 \uD83C\uDF1F");

            yesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    dialog.dismiss();
                    ReviewManager manager = ReviewManagerFactory.create(MainActivity.this);
                    Task<ReviewInfo> req =  manager.requestReviewFlow();
                    req.addOnSuccessListener(new OnSuccessListener<ReviewInfo>() {
                        @Override
                        public void onSuccess(ReviewInfo result) {

                            Task<Void> flow = manager.launchReviewFlow(MainActivity.this,result);
                            flow.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void result) {

                                    appDataPrefEditor.putBoolean(Constant.APP_RATED,true);
                                    appDataPrefEditor.apply();
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {

                            Toast.makeText(MainActivity.this, "Cannot rate InstaSave now", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            });

            noBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    appDataPrefEditor.putLong(Constant.APP_RATING_TIME,System.currentTimeMillis()+(5*24*60*60*1000));
                    appDataPrefEditor.apply();
                    finish();
                    dialog.dismiss();

                }
            });

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        }
        else if(scrollView.getScrollY()!=0)
        {
            scrollView.setScrollY(0);
        }
        else {

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
            }
            else
            {
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Tap back again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 3000);
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_insta:
                startActivity(new Intent(MainActivity.this, InstagramLoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            case R.id.action_bar_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }

}