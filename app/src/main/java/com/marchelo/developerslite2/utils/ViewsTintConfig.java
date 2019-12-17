package com.marchelo.developerslite2.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;

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
