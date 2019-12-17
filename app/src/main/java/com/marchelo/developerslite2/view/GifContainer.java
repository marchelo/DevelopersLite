package com.marchelo.developerslite2.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * @author Oleg Green
 * @since 06.09.15
 */
public class GifContainer extends FrameLayout implements ImageContainer {
    private static final String TAG = GifContainer.class.getSimpleName();
    private final ImageContainerDelegate icDelegate = new ImageContainerDelegate();

    @SuppressWarnings("FieldCanBeLocal")
    private boolean mMeasureAllChildren = true;
    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

    public GifContainer(Context context) {
        super(context);
    }

    public GifContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mMeasureAllChildren || child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        int maxImageWidth = getMeasuredWidth();
        int maxImageHeight = icDelegate.getMaxHeight() != -1 ? icDelegate.getMaxHeight() : getMeasuredHeight();

        int recommendedContainerHeight = Math.round(maxImageWidth / icDelegate.mAspectRatio);
        int finalContainerHeight = Math.min(maxImageHeight, recommendedContainerHeight);
        Log.d(TAG, "onMeasure(), maxImageWidth = " + maxImageWidth);
        Log.d(TAG, "onMeasure(), maxImageHeight = " + maxImageHeight);
        Log.d(TAG, "onMeasure(), recommendedContainerHeight = " + recommendedContainerHeight);
        Log.d(TAG, "onMeasure(), finalContainerHeight = " + finalContainerHeight);
        Log.d(TAG, "onMeasure(), -------------------------------------------------");

        setMeasuredDimension(getMeasuredWidth(), finalContainerHeight);

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                                    lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                                    lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    public void setAspectRatio(float aspectRatio) {
        icDelegate.setAspectRatio(aspectRatio);
    }

    @Override
    public float getAspectRatio() {
        return icDelegate.getAspectRatio();
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        icDelegate.setMaxHeight(maxHeight);
    }
}
