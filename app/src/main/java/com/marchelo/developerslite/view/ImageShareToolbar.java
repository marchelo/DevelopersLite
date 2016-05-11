package com.marchelo.developerslite.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.marchelo.developerslite.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Oleg Green
 * @since 18.01.16
 */
public class ImageShareToolbar extends LinearLayout {

    public static final int ANIMATION_DURATION = 300;
    @Bind(R.id.btn_share_image)
    View shareImageButton;

    @Bind(R.id.btn_save_image)
    View saveImageButton;
    private float mTranslationDelta;

    public ImageShareToolbar(Context context) {
        super(context);
    }

    public ImageShareToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageShareToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        mTranslationDelta = 1.3f * getResources().getDimensionPixelSize(R.dimen.post_share_button_size);
    }

    public void show() {
        animate().translationX(0);
    }

    public void hide() {
        animate().translationX(mTranslationDelta);
    }

    public void imageAvailable() {
        shareImageButton
                .animate()
                .setDuration(ANIMATION_DURATION)
                .alpha(1f);

        saveImageButton
                .animate()
                .setStartDelay(ANIMATION_DURATION / 2)
                .setDuration(ANIMATION_DURATION)
                .alpha(1f);
    }

    public void imageUnavailable() {
        saveImageButton.setAlpha(0f);
        shareImageButton.setAlpha(0f);
    }
}
