package com.marchelo.developerslite2;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.Util;
import com.marchelo.developerslite2.utils.DiskCache;
import com.marchelo.developerslite2.utils.StorageUtils;

import java.io.File;

import io.fabric.sdk.android.Fabric;

/**
 * @author Oleg Green
 * @since 25.08.15
 */
public class DevLifeApplication extends Application {
    private static DevLifeApplication sInstance;
    private static DiskCache sCache;
    private boolean mAutoLoad;

    public DevLifeApplication() {
        sInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        if (!BuildConfig.DEBUG) {
//            Fabric.with(this, new Crashlytics());
//        }
        //temp fix for problem with request cancellation in ION library
        Util.SUPRESS_DEBUG_EXCEPTIONS = true;

        sCache = new DiskCache(new File(getExternalCacheDir(), "gifs"));

        mAutoLoad = StorageUtils.isAutoLoadGifEnabled(this);
    }

    public static DevLifeApplication getInstance() {
        return sInstance;
    }

    public static DiskCache getCache() {
        return sCache;
    }

    public void setAutoLoadGifs(boolean autoLoad) {
        mAutoLoad = autoLoad;
    }

    public boolean isAutoLoadGifs() {
        return mAutoLoad;
    }
}