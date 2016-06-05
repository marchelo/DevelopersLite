package com.marchelo.developerslite.post_list;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.marchelo.developerslite.utils.ViewsTintConfig;
import com.marchelo.developerslite.view.ImageShareToolbar;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.lang.ref.WeakReference;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

/**
 * @author Oleg Green
 * @since 26.05.16
 */
public class GifImageHolder extends RecyclerView.ViewHolder {
    private static final String TAG = GifImageHolder.class.getSimpleName();
    private static final String SAVE_IMAGE_FILE_NAME_PREFIX = "image_";

    protected final Random mRandom = new Random();
    protected final Handler mUiHandler;
    protected final Context mContext;

    protected WeakReference<Future> mFutureRef = new WeakReference<>(null);
    protected Unbinder mUnBinder;
    protected String mGifUriString;

    @BindView(R.id.gif_image)
    protected GifImageButton gifImageView;

    @BindView(R.id.btn_play_pause)
    protected CompoundButton playPause;

    @BindView(R.id.progress_bar)
    protected ProgressBar progressBar;

    @BindView(R.id.btn_share_image_link)
    protected View shareImageLinkButton;

    @BindView(R.id.btn_image_toolbar)
    protected ImageShareToolbar imageToolbarView;

    @BindView(R.id.btn_save_gif_link)
    protected ImageButton saveLinkGifView;

    @BindView(R.id.view_load_fail)
    protected View failToLoadView;

    public GifImageHolder(View itemView, Handler handler) {
        super(itemView);
        mContext = itemView.getContext();
        mUiHandler = handler;

        mUnBinder = ButterKnife.bind(this, itemView);

        saveLinkGifView.setImageDrawable(ViewsTintConfig.getTinted(mContext,
                R.drawable.ic_favorite_white_selector, R.color.bookmark_button_tint_selector));
    }

    @OnClick(R.id.btn_share_image_link)
    protected void shareImageLink() {
        PostViewHelper.shareImageLink(mContext, mGifUriString);
    }

    @OnClick(R.id.btn_share_image)
    protected void shareImage() {
        PostViewHelper.shareImage(mContext, mGifUriString);
    }

    @OnClick(R.id.btn_save_image)
    protected void saveImage() {
        RxPermissions.getInstance(mContext)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        PostViewHelper.saveImage(mContext, mGifUriString, getGifNameForLocalSave());

                    } else {
                        showStoragePermissionsNotGrantedDialog();
                    }
                });
    }

    private void showStoragePermissionsNotGrantedDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.no_permission_to_save_file_dialog_title)
                .setMessage(R.string.no_permission_to_save_file_dialog_msg)
                .setPositiveButton(R.string.no_permission_to_save_file_dialog_ok_btn, (dialog, which) -> {
                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("package:" + mContext.getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    mContext.startActivity(i);
                })
                .setNegativeButton(R.string.no_permission_to_save_file_dialog_cancel_btn, null)
                .show();
    }

    @NonNull
    protected String getGifNameForLocalSave() {
        return SAVE_IMAGE_FILE_NAME_PREFIX + (mRandom.nextInt(10000) + 10000);
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

    private void refreshShareImageLinkButtonVisibility(Uri gifUri) {
        String uriScheme = gifUri.getScheme();
        if (uriScheme.equalsIgnoreCase("http") || uriScheme.equalsIgnoreCase("https")) {
            shareImageLinkButton.setVisibility(View.VISIBLE);
        } else {
            shareImageLinkButton.setVisibility(View.GONE);
        }
    }

    public void loadGifImage(Uri gifUri) {
        Log.d(TAG, "loadGifImage(), gif uri = " + gifUri);

        mGifUriString = gifUri.toString();
        saveLinkGifView.setVisibility(View.GONE);
        refreshShareImageLinkButtonVisibility(gifUri);

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
        cancelGifRequestIfNeeded();
        recycleGifDrawableIfNeeded();

        mUnBinder.unbind();
    }

    public void cancelGifRequestIfNeeded() {
        if (mFutureRef != null && mFutureRef.get() != null && !mFutureRef.get().isCancelled()) {
            mFutureRef.get().cancel();
        }
    }

    public void recycleGifDrawableIfNeeded() {
        Drawable drawable = gifImageView.getDrawable();
        Drawable bgDrawable = gifImageView.getBackground();
        if (drawable instanceof GifDrawable) {
            Log.d("test2", "release(), drawable is GifDrawable, recycling it");
            ((GifDrawable) drawable).recycle();
        }
        if (bgDrawable instanceof GifDrawable) {
            Log.d("test2", "release(), bgDrawable is GifDrawable, recycling it");
            ((GifDrawable) bgDrawable).recycle();

        }
    }
}
