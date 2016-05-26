package com.marchelo.developerslite;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.marchelo.developerslite.post_list.GifImageHolder;
import com.marchelo.developerslite.utils.IntentHelper;

/**
 * @author Oleg Green
 * @since 4.10.15
 */
public class GifViewerActivity extends AppCompatActivity {
    private static final String TAG = GifViewerActivity.class.getSimpleName();

    private GifImageHolder mGifImageHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_viewer);
        mGifImageHolder = new GifImageHolder(this, new Handler(), findViewById(R.id.gif_viewer_container));

        Uri gifUri = getIntent().getData();
        Log.d(TAG, "onCreate(), gifUri = " + gifUri);

        if (verifyGifImage(gifUri)) {
            mGifImageHolder.update(gifUri);
            mGifImageHolder.loadGifImage(gifUri);

        } else {
            IntentHelper.sendIntentWithoutApp(this, getIntent(), R.string.activity_gif_viewer_cannot_open_link);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGifImageHolder.release();
    }

    private boolean verifyGifImage(Uri gifUri) {
        return gifUri.toString().endsWith(".gif");
    }
}