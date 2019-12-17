package com.marchelo.developerslite2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.marchelo.developerslite2.post_list.GifImageHolder;
import com.marchelo.developerslite2.utils.IntentHelper;

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
        mGifImageHolder = new GifImageHolder(findViewById(R.id.gif_viewer_container), new Handler());

        Uri gifUri = getIntent().getData();
        Log.d(TAG, "onCreate(), gifUri = " + gifUri);

        if (verifyGifImage(gifUri)) {
            mGifImageHolder.loadGifImage(gifUri);

        } else {
            IntentHelper.sendIntentExcludingThisApp(this, getIntent(), R.string.activity_gif_viewer_cannot_open_link);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGifImageHolder.release();
    }

    private boolean verifyGifImage(Uri gifUri) {
        return gifUri != null && gifUri.toString().endsWith(".gif");
    }
}