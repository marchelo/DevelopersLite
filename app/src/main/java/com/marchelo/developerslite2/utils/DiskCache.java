package com.marchelo.developerslite2.utils;

import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Oleg Green
 * @since 06.09.15
 */
public class DiskCache {
    public static final int CACHE_FILE_COUNT_LIMIT = 7;
    private static final String TAG = DiskCache.class.getSimpleName();
    private static final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final File mCacheFolder;
    private boolean mIsWorking = true;

    public DiskCache(File gifFolder) {
        mCacheFolder = gifFolder;
        if (!mCacheFolder.exists()) {
            mIsWorking = mCacheFolder.mkdirs();
        }
    }

    public void putFileData(String name, byte[] data) {
        Log.d(TAG, "putFileData(), name = " + name);
        if (!mIsWorking) {
            Log.d(TAG, "putFileData(), skipping putting file cause deleting not working");
            return;
        }

        if (hasFile(name)) {
            Log.d(TAG, "putFileData(), skipping putting file = " + name);
            return;
        }

        mExecutor.execute(() -> putFileDataAsync(name, data));
    }

    private void putFileDataAsync(String name, byte[] data) {

        long startTime = System.currentTimeMillis();
        deleteOverLimitFiles();
        long duration = System.currentTimeMillis() - startTime;
        Log.d(TAG, "putFileDataAsync(), deleteOverLimitFiles() duration = " + duration);

        File fileTemp = new File(mCacheFolder, name.concat("-temp"));
        File file = new File(mCacheFolder, name);

        try {
            startTime = System.currentTimeMillis();
            IOUtils.write(data, new FileOutputStream(fileTemp, false));
            duration = System.currentTimeMillis() - startTime;
            Log.d(TAG, "putFileDataAsync(), IOUtils.write() duration = " + duration);

            startTime = System.currentTimeMillis();
            FileUtils.moveFile(fileTemp, file);
            duration = System.currentTimeMillis() - startTime;
            Log.d(TAG, "putFileDataAsync(), FileUtils.moveFile() duration = " + duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void deleteOverLimitFiles() {
        Log.d(TAG, "deleteOverLimitFiles()");
        if (mCacheFolder == null || !mCacheFolder.exists()) {
            return;
        }

        File[] filesArray = mCacheFolder.listFiles();
        if (filesArray != null && filesArray.length >= CACHE_FILE_COUNT_LIMIT) {
            List<File> files = Arrays.asList(filesArray);
            Collections.sort(files, (lhs, rhs) -> FileUtils.isFileNewer(lhs, rhs) ? 1 : -1);

            File fileToDelete = files.get(0);
            Log.d(TAG, "deleteOverLimitFiles(), fileToDelete = " + fileToDelete);
            mIsWorking = fileToDelete.delete();
        }
    }

    public synchronized boolean hasFile(String name) {
        boolean exists = new File(mCacheFolder, name).exists();
        Log.d(TAG, "hasFile(), = " + exists);
        return exists;
    }

    public synchronized Uri getFileUri(String name) {
        Uri uri = Uri.fromFile(new File(mCacheFolder, name));
        Log.d(TAG, "getFileUri(), name = " + name + ", result uri = " + uri);
        return uri;
    }

    @SuppressWarnings("unused")
    public synchronized byte[] getFile(String name) {
        Log.d(TAG, "getFile(), name = " + name);
        try {
            return IOUtils.toByteArray(new FileInputStream(new File(mCacheFolder, name)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileNameFromUrl(String gifUrl) {
        return gifUrl.replaceAll("\\W", "_");
    }
}
