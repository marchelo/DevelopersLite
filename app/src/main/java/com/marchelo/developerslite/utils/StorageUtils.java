package com.marchelo.developerslite.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Oleg Green
 * @since 20.01.16
 */
public final class StorageUtils {
    public static final String AUTO_LOAD_GIF_IMAGES_KEY = "AUTO_LOAD_GIF_IMAGES";
    private static final String CATEGORY_SELECTED_TYPE_KEY = "CATEGORY_SELECTED_TYPE";
    private static final String BEST_CATEGORY_SELECTED_TYPE_KEY = "BEST_CATEGORY_SELECTED_TYPE";

    private StorageUtils() {
        //hide
    }

    public static boolean isAutoLoadGifEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(AUTO_LOAD_GIF_IMAGES_KEY, false);
    }

    public static void setAutoLoadGifEnabled(Context context, boolean enabled) {
        getSharedPreferences(context).edit().putBoolean(AUTO_LOAD_GIF_IMAGES_KEY, enabled).commit();
    }

    public static int getBestCategorySelectedPos(Context context) {
        return getSharedPreferences(context).getInt(BEST_CATEGORY_SELECTED_TYPE_KEY, 0);
    }

    public static void setBestCategorySelectedPos(Context context, int selectedPos) {
        getSharedPreferences(context).edit().putInt(BEST_CATEGORY_SELECTED_TYPE_KEY, selectedPos).commit();
    }

    public static String getSelectedCategory(Context context) {
        return getSharedPreferences(context).getString(CATEGORY_SELECTED_TYPE_KEY, null);
    }

    public static void setSelectedCategory(Context context, String selectedCategory) {
        getSharedPreferences(context).edit().putString(CATEGORY_SELECTED_TYPE_KEY, selectedCategory).commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }
}
