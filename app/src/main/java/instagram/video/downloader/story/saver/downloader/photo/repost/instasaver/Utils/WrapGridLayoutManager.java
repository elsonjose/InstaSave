package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WrapGridLayoutManager extends GridLayoutManager {

    private static final String TAG = "WrapGridLayoutManager";
    public WrapGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public WrapGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }


    public WrapGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try{
            super.onLayoutChildren(recycler, state);
        }
        catch (IndexOutOfBoundsException e)
        {
            Log.i(TAG, "onLayoutChildren: IndexOutOfBoundsException ");
        }
    }
}
