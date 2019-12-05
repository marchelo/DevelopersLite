package com.marchelo.developerslite.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.marchelo.developerslite.R;

/**
 * @author Oleg Green
 * @since 18.10.15
 */
public class ProgressBarTint extends ProgressBar {

    private @ColorInt int mTintColor;

    public ProgressBarTint(Context context) {
        super(context);
        mTintColor = Color.BLUE;
    }

    public ProgressBarTint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressBarTint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressBarTint(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressBarTint, 0, 0);

        try {
            mTintColor = a.getColor(R.styleable.ProgressBarTint_colorTint, Color.BLUE);
            setProgressDrawable(getProgressDrawable());
            setIndeterminateDrawable(getIndeterminateDrawable());
        } finally {
            a.recycle();
        }
    }

    public void setProgressDrawable(Drawable drawable) {
        super.setProgressDrawable(drawable);
        if (drawable != null) {
            drawable.setColorFilter(mTintColor, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setIndeterminateDrawable(Drawable drawable) {
        super.setIndeterminateDrawable(drawable);
        if (drawable != null) {
            drawable.setColorFilter(mTintColor, PorterDuff.Mode.SRC_IN);
        }
    }
}
