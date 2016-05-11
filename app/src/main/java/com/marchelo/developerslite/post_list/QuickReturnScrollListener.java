package com.marchelo.developerslite.post_list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author Oleg Green
 * @since 30.08.15
 */
public class QuickReturnScrollListener extends RecyclerView.OnScrollListener {
    static final float MINIMUM = 25;
    int scrollDist = 0;
    boolean isVisible = true;
    private final ToolbarListener mListener;

    public QuickReturnScrollListener(@NonNull ToolbarListener listener) {
        mListener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (isVisible && scrollDist > MINIMUM) {
            mListener.onUp();
            scrollDist = 0;
            isVisible = false;
        }
        else if (!isVisible && scrollDist < -MINIMUM) {
            mListener.onDown();
            scrollDist = 0;
            isVisible = true;
        }
        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }
    }

    public interface ToolbarListener {
        void onDown();
        void onUp();
    }
}
