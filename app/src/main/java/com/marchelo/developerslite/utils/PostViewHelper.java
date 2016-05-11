package com.marchelo.developerslite.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.marchelo.developerslite.DevLifeApplication;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.view.ExpandableTextView;
import com.marchelo.developerslite.utils.LoadGifImageReactor.OnImageSizeDefinedCallback;
import com.marchelo.developerslite.utils.LoadGifImageReactor.LoadResultCallback;
import com.marchelo.developerslite.view.ImageShareToolbar;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import pl.droidsonroids.gif.GifImageButton;

/**
 * @author Oleg Green
 * @since 30.08.15
 */
public final class PostViewHelper {
    private static final String TAG = PostViewHelper.class.getSimpleName();
    public static final String PICTURES_FOLDER_NAME = "DevelopersLite";
    public static final int AWESOME_RATING = 500;
    public static final int GREAT_RATING = 200;
    public static final int GOOD_RATING = 50;
    public static final int SO_SO_RATING = -100;

    private PostViewHelper() {
        //hidden
    }

    @Nullable
    public static Future<byte[]>
    loadGifAsync(GifImageButton gifImage, CompoundButton playPause, ProgressBar progressBar, ImageShareToolbar shareImageLayout,
                 String gifUrl, Handler mUiHandler, OnImageSizeDefinedCallback sizeDefinedCallback,
                 LoadResultCallback loadResultCallback, boolean autoStartGif) {

        Runnable action = () -> progressBar.setIndeterminate(true);
        progressBar.postDelayed(action, 500);
        progressBar.setTag(action);

        playPause.setChecked(autoStartGif);

        if (gifUrl == null) {
            Log.e("PostViewHelper", "loadGifAsync(), gif url is null");
            return null;
        }

        String fileName = DiskCache.getFileNameFromUrl(gifUrl);
        DiskCache cache = DevLifeApplication.getCache();
        String gifURL = (!cache.hasFile(fileName)) ? gifUrl : cache.getFileUri(fileName).toString();

        LoadGifImageReactor callback = new LoadGifImageReactor(fileName, gifImage, playPause,
                progressBar, shareImageLayout, sizeDefinedCallback, loadResultCallback);

        return Ion.with(gifImage.getContext())
                .load(gifURL)
                .progress((downloaded, total) -> mUiHandler.post(() -> {
                    progressBar.setIndeterminate(false);
                    if (progressBar.getTag() != null && progressBar.getTag() instanceof Runnable) {
                        progressBar.removeCallbacks((Runnable) progressBar.getTag());
                    }
                    progressBar.setMax((int) total);
                    progressBar.setProgress((int) downloaded);
                }))
                .asByteArray()
                .setCallback(callback);
    }

    public static void initCommonViews(ExpandableTextView desc, TextView author, TextView rating, Post post) {
        initDescriptionView(desc, post);
        initAuthorView(author, post);
        initRatingView(rating, post);
    }

    public static void initAuthorView(TextView view, Post post) {
        view.setText(view.getContext().getString(R.string.post_item_author, post.getAuthor()));
    }

    public static void initRatingView(TextView view, Post post) {
        int votes = post.getVotes();
        ViewsTintConfig.getTinted(view.getContext(), R.drawable.ic_favorite_black_18dp, R.color.colorPrimaryVeryDark);
        @DrawableRes int endDrawableId = (votes > AWESOME_RATING) ? R.drawable.ic_favorite_black_18dp : 0;
        view.setCompoundDrawablesWithIntrinsicBounds(0, 0, endDrawableId, 0);

        @ColorRes int textColor;
        if (votes >= GREAT_RATING) {
            textColor = R.color.colorPrimaryVeryDark;
        } else if (votes >= GOOD_RATING) {
            textColor = R.color.colorPrimary;
        } else if (votes >= SO_SO_RATING) {
            textColor = R.color.gray_color;
        } else {
            textColor = R.color.light_gray_color;
        }

        int colorValue = view.getContext().getResources().getColor(textColor);
        String ratingString = formatRating(votes);
        String ratingFullText = view.getContext().getString(R.string.post_item_rating, ratingString);

        Spannable ratingFullTextSpannable = new SpannableString(ratingFullText);
        ratingFullTextSpannable.setSpan(
                new ForegroundColorSpan(colorValue),
                ratingFullText.length() - ratingString.length() - 2,
                ratingFullText.length() - 1,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        view.setText(ratingFullTextSpannable);
    }

    public static void initDescriptionView(ExpandableTextView descriptionView, Post post) {
        descriptionView.setText(post.getDescription().trim());
    }

    public static String formatRating(int votes) {
        return votes > 0 ? "+" + votes : "" + votes;
    }

    public static boolean showShareImageLinkHint(Context context) {
        Toast.makeText(context, R.string.btn_share_image_link_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    public static boolean showShareImageHint(Context context) {
        Toast.makeText(context, R.string.btn_share_image_link_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    public static boolean showSaveImageHint(Context context) {
        Toast.makeText(context, R.string.btn_share_image_link_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    public static void shareImageLink(Context context, String url) {
        if (!IntentHelper.shareImageLink(context, url)) {
            IntentHelper.onNoActivityFoundToHandleIntent(context, R.string.no_app_to_share_link_found);
        }
    }

    public static void sharePostLink(Context context, long postId) {
        if (!IntentHelper.sharePostLink(context, postId)) {
            IntentHelper.onNoActivityFoundToHandleIntent(context, R.string.no_app_to_share_link_found);
        }
    }

    public static void shareImage(@NonNull Context context, @NonNull String gifUrl) {
        shareImageAndDescription(context, gifUrl, null);
    }

    public static void sharePost(@NonNull Context context, @NonNull Post post) {
        shareImageAndDescription(context, post.getGifURL(), post.getDescription());
    }

    public static void shareImageAndDescription(@NonNull Context context, @NonNull String gifUrl, @Nullable String description) {
        String fileName = DiskCache.getFileNameFromUrl(gifUrl);
        Uri fileUri = DevLifeApplication.getCache().getFileUri(fileName);
        if (new File(fileUri.getPath()).exists()) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("image/gif");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            if (!TextUtils.isEmpty(description)) {
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, description);
            }

            try {
                context.startActivity(sharingIntent);

            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "shareLink(), no activity found to handle intent [" + sharingIntent + "]");
            }

        } else {
            Log.d(TAG, "shareImageAndDescription(), cannot share cause wasn't found file in cache");
            Toast.makeText(context, R.string.share_image_failed, Toast.LENGTH_LONG).show();
        }
    }

    public static void saveImage(@NonNull Context context, @NonNull Post post) {
        new SaveFileToStorage(context, post.getGifURL(), String.valueOf(post.getPostId())).execute();
    }

    public static void saveImage(@NonNull Context context, @NonNull String gifUrl, @NonNull String newFileName) {
        new SaveFileToStorage(context, gifUrl, newFileName).execute();
    }

    private static void addImageToMediaLibrary(Context context, File destinationFile) {
        MediaScannerConnection.scanFile(context.getApplicationContext(),
                new String[]{destinationFile.toString()}, new String[]{"image/*"},
                (path, uri) -> Log.i(TAG, "Media Scanned " + path + ", uri=" + uri));
    }

    private static class SaveFileToStorage extends AsyncTask<Void, String, Pair<Exception, File>> {
        private final Context mContext;
        private String mGifURL;
        private String mSaveFileName;

        public SaveFileToStorage(@NonNull Context context, @NonNull String gifUrl, @NonNull String newFileName) {
            mContext = context;
            mGifURL = gifUrl;
            mSaveFileName = newFileName;
        }

        @Override
        protected Pair<Exception, File> doInBackground(Void... params) {
            String fileName = DiskCache.getFileNameFromUrl(mGifURL);
            Uri fileUri = DevLifeApplication.getCache().getFileUri(fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                File sourceFile = new File(fileUri.getPath());
                if (sourceFile.exists()) {
                    inputStream = new FileInputStream(sourceFile);

                } else {
                    URL url = new URL(mGifURL);
                    inputStream = url.openStream();
                    Log.i(TAG, "saveImage(), file wasn't found in cache, need downloading");
                    publishProgress(mContext.getString(R.string.save_file_start_downloading));
                }
                File externalPicturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File developersLitePicturesFolder = new File(externalPicturesFolder, PICTURES_FOLDER_NAME);
                //noinspection ResultOfMethodCallIgnored
                developersLitePicturesFolder.mkdirs();
                String destinationFileName = mContext.getString(R.string.save_file_file_name, mSaveFileName);
                File destinationFile = new File(developersLitePicturesFolder, destinationFileName);
                outputStream = new FileOutputStream(destinationFile);
                IOUtils.copy(inputStream, outputStream);

                addImageToMediaLibrary(mContext, destinationFile);

                Log.i(TAG, "saveImage(), destinationFile = " + destinationFile);
                return new Pair<>(null, destinationFile);

            } catch (IOException e) {
                Log.e(TAG, "saveImage(), issue when saving file. ", e);
                return new Pair<>(e, null);

            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values != null && !TextUtils.isEmpty(values[0])) {
                Toast.makeText(mContext, values[0], Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Pair<Exception, File> result) {
            Exception exception = result.first;
            File savedFile = result.second;
            if (exception != null) {
                Toast.makeText(mContext, R.string.failed_to_save_image, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(mContext, mContext.getString(R.string.image_is_success_saved, savedFile),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
