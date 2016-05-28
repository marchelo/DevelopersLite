package com.marchelo.developerslite.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.marchelo.developerslite.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Oleg Green
 * @since 12.09.15
 */
public class GifAndTextContainer extends FrameLayout implements ImageContainer {
    private static final String TAG = GifAndTextContainer.class.getSimpleName();
    private final ImageContainerDelegate icDelegate = new ImageContainerDelegate();

    @BindView(R.id.tv_description) ExpandableTextView mExpandableTextView;
    @BindView(R.id.gif_container) ImageContainer mImageContainer;

    public GifAndTextContainer(Context context) {
        super(context);
    }

    public GifAndTextContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifAndTextContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GifAndTextContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        if (mExpandableTextView == null) {
            throw new IllegalStateException(ExpandableTextView.class.getSimpleName() + " should be in layout with id = 'tv_description'");
        } else if (mImageContainer == null) {
            throw new IllegalStateException(ImageContainer.class.getSimpleName() + " should be in layout with id = 'gif_container'");
        } else if (getChildCount() > 2) {
            throw new IllegalStateException("There should be no child views except " + ImageContainer.class.getSimpleName()
                    + " and " + ExpandableTextView.class.getSimpleName());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();

        final int parentTop = getPaddingTop();

        int currentBottom = layoutChildren(mExpandableTextView, parentLeft, parentTop, parentRight);
        layoutChildren((View) mImageContainer, parentLeft, parentTop + currentBottom, parentRight);
    }

    public int layoutChildren(View child, int left, int top, int right) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();

        int childLeft;
        int childTop;

        childLeft = left + (right - left - width) / 2 + lp.leftMargin - lp.rightMargin;
        childTop = top + lp.topMargin;

        child.layout(childLeft, childTop, childLeft + width, childTop + height);

        return childTop + height + lp.bottomMargin;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandableTextHeightSpec = MeasureSpec.makeMeasureSpec(icDelegate.getMaxHeight(), MeasureSpec.AT_MOST);
        measureChild(mExpandableTextView, widthMeasureSpec, expandableTextHeightSpec);
        int textViewHeight = mExpandableTextView.getMeasuredHeight();
        Log.d(TAG, "onMeasure(), textViewHeight = " + textViewHeight);

        View imageContainerView = (View) mImageContainer;
        int imageContainerHeightSpec;
        int maxHeight = icDelegate.getMaxHeight() - textViewHeight;
        if (icDelegate.getMaxHeight() != -1) {
            mImageContainer.setMaxHeight(maxHeight);
            imageContainerHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        } else {
            imageContainerHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        Log.d(TAG, "onMeasure(), imageContainerHeightSpec = " + MeasureSpec.toString(imageContainerHeightSpec));

        imageContainerView.measure(widthMeasureSpec, imageContainerHeightSpec);

        int imageContainerViewHeight = imageContainerView.getMeasuredHeight();
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), textViewHeight + imageContainerViewHeight);

        //final measure
        /////
        final MarginLayoutParams lp = (MarginLayoutParams) mExpandableTextView.getLayoutParams();

        final int width = Math.max(0, getMeasuredWidth() - lp.leftMargin - lp.rightMargin);
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);


        final int height = Math.max(0, getMeasuredHeight() - lp.topMargin - lp.bottomMargin);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        //////
        measureChild(mExpandableTextView, childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void setAspectRatio(float aspectRatio) {
        mImageContainer.setAspectRatio(aspectRatio);
    }

    @Override
    public float getAspectRatio() {
        return mImageContainer.getAspectRatio();
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        icDelegate.setMaxHeight(maxHeight);
    }
}
