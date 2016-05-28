package com.marchelo.developerslite.details;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.utils.Config;
import com.marchelo.developerslite.utils.DeviceUtils;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.marchelo.developerslite.view.ImageContainer;

import java.lang.ref.WeakReference;
import java.text.DateFormat;

import butterknife.BindView;

/**
 * @author Oleg Green
 * @since 26.05.16
 */
public class PostDetailsViewHolder extends APostViewHolder {
    public final DateFormat DATE_TIME_FORMATTER = Config.getDateFormat();

    @BindView(R.id.image_container)
    protected ImageContainer imageContainer;

    @BindView(R.id.header_comments_view)
    public CompoundButton commentsHeaderView;


    public PostDetailsViewHolder(View itemView, Handler handler, float aspectRatio) {
        super(itemView, handler);
        configureImageContainerSize(aspectRatio);
    }

    public void loadPostImage(Post post) {
        mPost = post;
        mGifUriString = post.getGifURL();

        PostViewHelper.initCommonViews(descriptionView, authorView, ratingView, post);
        dateView.setText(DATE_TIME_FORMATTER.format(post.getDate()));

        playPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        Future future = PostViewHelper.loadGifAsync(
                gifImageView,
                playPause,
                progressBar,
                null,
                post.getGifURL(),
                mUiHandler,
                this::onAspectRatioDetected,
                new LoadGifImageReactor.LoadResultCallback() {
                    @Override
                    public void onLoadSuccessful() {}

                    @Override
                    public void onLoadFailed(@Nullable Exception exception) {
                        failToLoadView.setVisibility(View.VISIBLE);
                        playPause.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                },
                true);

        mFutureRef = new WeakReference<>(future);
    }

    private void onAspectRatioDetected(float aspectRatio) {
        if (Math.abs(imageContainer.getAspectRatio() - aspectRatio) > 0.01) {
            imageContainer.setAspectRatio(aspectRatio);
            imageContainer.invalidate();
        }
    }

    public void configureImageContainerSize(float aspectRatio) {
        imageContainer.setAspectRatio(aspectRatio);

        int verticalMargin = mContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        int toolbarHeight = mContext.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        int maxHeight = DeviceUtils.getDeviceHeight(mContext) - verticalMargin * 2
                - toolbarHeight - DeviceUtils.getStatusBarHeight(mContext);
        imageContainer.setMaxHeight(maxHeight);
    }
}