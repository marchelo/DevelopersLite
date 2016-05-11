package com.marchelo.developerslite.post_list;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import pl.droidsonroids.gif.GifDrawable;

/**
 * @author Oleg Green
 * @since 17.09.15
 */
public class LoadPreviewImageCallback implements Target {
    private final PostViewHolder mHolder;

    public LoadPreviewImageCallback(PostViewHolder holder) {
        mHolder = holder;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (!(mHolder.gifImageView.getDrawable() instanceof GifDrawable)) {
            mHolder.gifImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
}
