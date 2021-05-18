package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.BottomSheet;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.ref.WeakReference;

import instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.R;


public class WebBottomSheetView extends BottomSheetDialogFragment {

    String bookUrl;
    WeakReference<View> progressbar;
    WeakReference<View> parentView;

    public WebBottomSheetView() {
    }

    public WebBottomSheetView(String s, View view, View clickedView) {
        bookUrl = s;
        progressbar = new WeakReference<>(view);
        parentView = new WeakReference<>(clickedView);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View bookView = inflater.inflate(R.layout.web_view,container,false);
        android.webkit.WebView bookWebView = bookView.findViewById(R.id.web_web_view);
        WebSettings webSettings = bookWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        TextView bookTitleTextView = bookView.findViewById(R.id.web_view_title);
        bookTitleTextView.setText(bookUrl);
        ImageButton bookTitleBackButton = bookView.findViewById(R.id.web_view_actionbar_back_button);
        final ProgressBar bookViewProgressbar = bookView.findViewById(R.id.web_view_progressbar);
        bookWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                bookViewProgressbar.setProgress(newProgress);
                if(newProgress==100)
                {
                    bookViewProgressbar.setVisibility(View.GONE);
                    progressbar.get().setVisibility(View.GONE);
                    parentView.get().setEnabled(true);
                }
            }
        });

        bookTitleBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();

            }
        });
        bookWebView.setWebViewClient(new Callback());
        bookWebView.loadUrl(bookUrl);

        return bookView;
    }


    private class Callback extends WebViewClient
    {
        @Override
        public boolean shouldOverrideKeyEvent(android.webkit.WebView view, KeyEvent event) {
            return false;
        }
    }

}
