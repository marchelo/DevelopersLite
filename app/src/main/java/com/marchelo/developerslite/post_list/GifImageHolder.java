package com.marchelo.developerslite.post_list;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.marchelo.developerslite.view.ImageShareToolbar;

import java.lang.ref.WeakReference;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import pl.droidsonroids.gif.GifImageButton;

/**
 * @author Oleg Green
 * @since 26.05.16
 */
public class GifImageHolder {
    private static final String TAG = GifImageHolder.class.getSimpleName();
    public static final String SAVE_IMAGE_FILE_NAME_PREFIX = "image_";

    private final Random mRandom = new Random();
    private final Handler mUiHandler;
    private final Context mContext;

    private WeakReference<Future> mFutureRef = new WeakReference<>(null);
    private Uri mGifUri;

    @Bind(R.id.gif_image)
    GifImageButton gifImageView;

    @Bind(R.id.btn_play_pause)
    CompoundButton playPause;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.btn_share_image_link)
    View shareImageLinkButton;

    @Bind(R.id.btn_image_toolbar)
    ImageShareToolbar imageToolbarView;

    @Bind(R.id.btn_save_gif_link)
    View saveLinkGifView;

    @Bind(R.id.view_load_fail)
    View failToLoadView;

    public GifImageHolder(Context context, Handler handler, View itemView) {
        mContext = context;
        mUiHandler = handler;
        ButterKnife.bind(itemView);
    }

    public void update(Uri gifUri) {
        mGifUri = gifUri;
        refreshShareImageLinkButtonVisibility();
    }

    @OnClick(R.id.btn_share_image_link)
    protected void shareImageLink() {
        PostViewHelper.shareImageLink(mContext, mGifUri.toString());
    }

    @OnClick(R.id.btn_share_image)
    protected void shareImage() {
        PostViewHelper.shareImageAndDescription(mContext, mGifUri.toString(), null);
    }

    @OnClick(R.id.btn_save_image)
    protected void saveImage() {
        String newFileName = SAVE_IMAGE_FILE_NAME_PREFIX + (mRandom.nextInt(10000) + 10000);
        PostViewHelper.saveImage(mContext, mGifUri.toString(), newFileName);
    }

    @OnLongClick(R.id.btn_share_image_link)
    boolean showShareImageLinkHint() {
        return PostViewHelper.showShareImageLinkHint(mContext);
    }

    @OnLongClick(R.id.btn_share_image)
    boolean showShareImageHint() {
        return PostViewHelper.showShareImageHint(mContext);
    }

    @OnLongClick(R.id.btn_save_image)
    boolean showSaveImageHint() {
        return PostViewHelper.showSaveImageHint(mContext);
    }

    private void refreshShareImageLinkButtonVisibility() {
        String uriScheme = mGifUri.getScheme();
        if (uriScheme.equalsIgnoreCase("http") || uriScheme.equalsIgnoreCase("https")) {
            shareImageLinkButton.setVisibility(View.VISIBLE);
        } else {
            shareImageLinkButton.setVisibility(View.GONE);
        }
    }

    public void loadGifImage(Uri gifUri) {
        Log.d(TAG, "loadGifImage(), gif uri = " + gifUri);

        saveLinkGifView.setVisibility(View.GONE);
        imageToolbarView.hide();
        imageToolbarView.imageUnavailable();
        playPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                progressBar.setVisibility(View.VISIBLE);
                imageToolbarView.hide();

            } else {
                imageToolbarView.show();
            }
        });

        Future future = PostViewHelper.loadGifAsync(
                gifImageView,
                playPause,
                progressBar,
                imageToolbarView,
                gifUri.toString(),
                mUiHandler,
                null,
                new LoadGifImageReactor.LoadResultCallback() {
                    @Override
                    public void onLoadSuccessful() {
                        imageToolbarView.imageAvailable();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Exception exception) {
                        if (failToLoadView != null) {
                            failToLoadView.setVisibility(View.VISIBLE);
                        }
                        playPause.setVisibility(View.INVISIBLE);
                    }
                },
                true);

        mFutureRef = new WeakReference<>(future);
    }

    public void release() {
        ButterKnife.unbind(this);

        if (mFutureRef.get() != null) {
            mFutureRef.get().cancel();
        }
    }
}
