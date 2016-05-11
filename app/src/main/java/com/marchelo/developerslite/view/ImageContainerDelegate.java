package com.marchelo.developerslite.view;

import android.util.Log;

public class ImageContainerDelegate {
    private static final String TAG = ImageContainerDelegate.class.getSimpleName();

    float mAspectRatio = 1f;
    int mMaxHeight = -1;

    public void setAspectRatio(float aspectRatio) {
        Log.d(TAG, "setAspectRatio() = " + aspectRatio);
        mAspectRatio = aspectRatio;
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    public void setMaxHeight(int maxHeight) {
        Log.d(TAG, "setMaxHeight() = " + maxHeight);
        mMaxHeight = maxHeight;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }
}