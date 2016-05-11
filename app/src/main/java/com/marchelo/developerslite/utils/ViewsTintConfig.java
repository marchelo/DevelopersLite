package com.marchelo.developerslite.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * @author Oleg Green
 * @since 16.01.16
 */
public final class ViewsTintConfig {

    public static Drawable getTinted(Context context, @DrawableRes int drawableId, @ColorRes int colorId) {
        //noinspection deprecation
        Drawable drawable = DrawableCompat.wrap(context.getResources().getDrawable(drawableId));
        DrawableCompat.setTintList(drawable, context.getResources().getColorStateList(colorId));
        return drawable;
    }
}
