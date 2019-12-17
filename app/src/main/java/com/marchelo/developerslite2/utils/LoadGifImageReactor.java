package com.marchelo.developerslite2.utils;

import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;
import com.marchelo.developerslite2.DevLifeApplication;
import com.marchelo.developerslite2.view.ImageShareToolbar;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

/**
 * @author Oleg Green
 * @since 11.10.15
 */
public class LoadGifImageReactor implements FutureCallback<byte[]> {
    private static final String TAG = LoadGifImageReactor.class.getSimpleName();
    private final GifImageButton mGifImage;
    private final CompoundButton mPlayPause;
    private final ImageShareToolbar mShareImageLayout;
    private final ProgressBar mProgressBar;
    private final OnImageSizeDefinedCallback mOnSizeDefinedCallback;
    private final LoadResultCallback mLoadCallback;
    private final String mFileName;

    public interface OnImageSizeDefinedCallback {
        void onImageSizeDefined(float aspectRatio);
    }

    public interface LoadResultCallback {
        void onLoadSuccessful();
        void onLoadFailed(@Nullable Exception exception);
    }

    public LoadGifImageReactor(String fileName, GifImageButton gifImage, CompoundButton playPause,
                               ProgressBar progressBar, ImageShareToolbar shareImageLayout,
                               OnImageSizeDefinedCallback sizeDefinedCallback,
                               LoadResultCallback loadCallback) {
        mFileName = fileName;
        mGifImage = gifImage;
        mPlayPause = playPause;
        mProgressBar = progressBar;
        mShareImageLayout = shareImageLayout;
        mOnSizeDefinedCallback = sizeDefinedCallback;
        mLoadCallback = loadCallback;
    }

    @Override
    public void onCompleted(Exception e, byte[] result) {
        Log.d(TAG, "onCompleted()");

        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setIndeterminate(false);

        if (mProgressBar.getTag() != null && mProgressBar.getTag() instanceof Runnable) {
            mProgressBar.removeCallbacks((Runnable) mProgressBar.getTag());
        }

        if (e != null) {
            if (!(e instanceof CancellationException)) {
                Log.e(TAG, "onCompleted() with exception", e);
                onLoadFailed(e);
            } else {
                Log.d(TAG, "onCompleted() after cancellation");
            }
            return;
        }
        final GifDrawable gifDrawable;
        try {
            if (result == null) {
                Log.e(TAG, "result == null");
                onLoadFailed(null);

            } else {
                gifDrawable = new GifDrawable(result);
                mGifImage.setImageDrawable(gifDrawable);

                DevLifeApplication.getCache().putFileData(mFileName, result);
                if (mOnSizeDefinedCallback != null) {
                    mOnSizeDefinedCallback.onImageSizeDefined(getDrawableAspectRatio(gifDrawable));
                }

                if (!mPlayPause.isChecked()) {
                    gifDrawable.stop();
                }
                mPlayPause.setOnCheckedChangeListener((buttonView, isChecked)
                        -> setGifDrawableRunning(!isChecked, gifDrawable));
                setGifDrawableRunning(!mPlayPause.isChecked(), gifDrawable);

                onLoadSuccessful();
            }
        } catch (IOException e1) {
            Log.e(TAG, "Failed to create Gif Drawable", e1);
            onLoadFailed(e1);
        }
    }

    public void onLoadSuccessful() {
        if (mLoadCallback != null) {
            mLoadCallback.onLoadSuccessful();
        }
    }

    public void onLoadFailed(Exception exception) {
        if (mLoadCallback != null) {
            mLoadCallback.onLoadFailed(exception);
        }
    }

    private float getDrawableAspectRatio(Drawable drawable) {
        return (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
    }

    private void setGifDrawableRunning(boolean running, GifDrawable gifDrawable) {
        if (running) {
            if (mShareImageLayout != null) {
                mShareImageLayout.show();
            }
            gifDrawable.stop();
        } else {
            if (mShareImageLayout != null) {
                mShareImageLayout.hide();
            }
            gifDrawable.start();
        }
    }
}