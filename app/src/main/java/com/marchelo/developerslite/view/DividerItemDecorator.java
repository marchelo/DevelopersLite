package com.marchelo.developerslite.view;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Oleg Green
 * @since 16.08.15
 */
public class DividerItemDecorator extends RecyclerView.ItemDecoration {
    private final int mDividerSize;

    public DividerItemDecorator(int dividerSize) {
        mDividerSize = dividerSize;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int spanCount = getSpanCount(parent);

        if (parent.getChildAdapterPosition(view) / spanCount != 0) {
            outRect.top = mDividerSize;
        }

        if (parent.getChildAdapterPosition(view) % spanCount != 0) {
            //noinspection SuspiciousNameCombination
            outRect.left = mDividerSize;
        }
    }

    private int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        return (layoutManager instanceof GridLayoutManager) ? ((GridLayoutManager)layoutManager).getSpanCount() : 1;
    }
}