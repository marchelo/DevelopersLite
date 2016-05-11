package com.marchelo.developerslite;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.utils.IntentHelper;
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
 * @since 4.10.15
 */
public class GifViewerActivity extends AppCompatActivity {
    private static final String TAG = GifViewerActivity.class.getSimpleName();
    public static final String SAVE_IMAGE_FILE_NAME_PREFIX = "image_";

    private final Random mRandom = new Random();
    private WeakReference<Future> mFutureRef = new WeakReference<>(null);
    private Handler mUiHandler;
    private Uri mGifUri;

    @Bind(R.id.gif_image) GifImageButton            gifImageView;
    @Bind(R.id.btn_play_pause) CompoundButton       playPause;
    @Bind(R.id.progress_bar) ProgressBar            progressBar;
    @Bind(R.id.btn_share_image_link) View           shareImageLinkButton;
    @Bind(R.id.btn_image_toolbar) ImageShareToolbar imageToolbarView;
    @Bind(R.id.btn_save_gif_link) View              saveLinkGifView;
    @Bind(R.id.view_load_fail) View                 failToLoadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_viewer);
        ButterKnife.bind(this);

        mUiHandler = new Handler();
        mGifUri = getIntent().getData();
        Log.d(TAG, "onCreate(), mGifUri = " + mGifUri);

        if (verifyGifImage()) {
            loadGifImage();

        } else {
            IntentHelper.sendIntentWithoutApp(this, getIntent(), R.string.activity_gif_viewer_cannot_open_link);
        }
    }

    @OnClick(R.id.btn_share_image_link)
    protected void shareImageLink() {
        PostViewHelper.shareImageLink(this, mGifUri.toString());
    }

    @OnClick(R.id.btn_share_image)
    protected void shareImage() {
        PostViewHelper.shareImageAndDescription(this, mGifUri.toString(), null);
    }

    @OnClick(R.id.btn_save_image)
    protected void saveImage() {
        String newFileName = SAVE_IMAGE_FILE_NAME_PREFIX + (mRandom.nextInt(10000) + 10000);
        PostViewHelper.saveImage(this, mGifUri.toString(), newFileName);
    }

    @OnLongClick(R.id.btn_share_image_link)
    boolean showShareImageLinkHint() {
        return PostViewHelper.showShareImageLinkHint(this);
    }

    @OnLongClick(R.id.btn_share_image)
    boolean showShareImageHint() {
        return PostViewHelper.showShareImageHint(this);
    }

    @OnLongClick(R.id.btn_save_image)
    boolean showSaveImageHint() {
        return PostViewHelper.showSaveImageHint(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);

        if (mFutureRef.get() != null) {
            mFutureRef.get().cancel();
        }
    }

    private boolean verifyGifImage() {
        return mGifUri.toString().endsWith(".gif");
    }

    private void loadGifImage() {
        Log.d(TAG, "loadGifImage(), gif uri = " + mGifUri);

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

        refreshShareImageLinkButtonVisibility();

        Future future = PostViewHelper.loadGifAsync(
                gifImageView,
                playPause,
                progressBar,
                imageToolbarView,
                mGifUri.toString(),
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

    private void refreshShareImageLinkButtonVisibility() {
        String uriScheme = mGifUri.getScheme();
        if (uriScheme.equalsIgnoreCase("http") || uriScheme.equalsIgnoreCase("https")) {
            shareImageLinkButton.setVisibility(View.VISIBLE);
        } else {
            shareImageLinkButton.setVisibility(View.GONE);
        }
    }
}