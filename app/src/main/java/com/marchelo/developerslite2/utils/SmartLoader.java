package com.marchelo.developerslite2.utils;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;

/**
 * @author Oleg Green
 * @since 09.05.16
 */
public abstract class SmartLoader<DataType> extends AsyncTaskLoader<DataType> {
    private DataType mData;

    public SmartLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null && !takeContentChanged()) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(DataType data) {
        mData = data;
        super.deliverResult(data);
    }
}