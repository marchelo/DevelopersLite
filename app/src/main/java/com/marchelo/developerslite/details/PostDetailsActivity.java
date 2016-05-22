package com.marchelo.developerslite.details;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.CommentsListHolder;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.network.ApiFactory;
import com.marchelo.developerslite.utils.DeviceUtils;
import com.marchelo.developerslite.utils.IntentHelper;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.marchelo.developerslite.view.ImageContainer;
import com.marchelo.developerslite.view.ExpandableTextView;
import com.marchelo.developerslite.view.ImageShareToolbar;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import icepick.Icepick;
import icepick.State;
import pl.droidsonroids.gif.GifImageButton;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Oleg Green
 * @since 24.08.15
 */
public class PostDetailsActivity extends AppCompatActivity {
    private static final String TAG = PostDetailsActivity.class.getSimpleName();
    private static final String POST_TO_SHOW_TAG = "POST_TO_SHOW";
    public static final String IMAGE_RATIO_TAG = "ratio";

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private final ApiFactory.ApiPostById mApi = ApiFactory.postByIdApi();
    private WeakReference<Future> mFutureRef = new WeakReference<>(null);
    private LoadGifImageReactor.LoadResultCallback mLoadResultCallback;
    private Handler mUiHandler;

    @Bind(R.id.toolbar_title) TextView              mTitle;
    @Bind(R.id.gif_image) GifImageButton            gifImageView;
    @Bind(R.id.tv_description) ExpandableTextView   descriptionView;
    @Bind(R.id.tv_author) TextView                  authorView;
    @Bind(R.id.tv_rating) TextView                  ratingView;
    @Bind(R.id.btn_play_pause) CompoundButton       playPause;
    @Bind(R.id.progress_bar) ProgressBar            progressBar;
    @Bind(R.id.image_container) ImageContainer      imageContainer;
    @Bind(R.id.btn_image_toolbar) ImageShareToolbar imageToolbarView;
    @Bind(R.id.view_load_fail) View                 failToLoadView;

    @State Post mPost;
    private CommentsAdapter mCommentsAdapter;

    public static void startActivity(Activity activity, Post postToShow, View description,
                                     View author, View rating, float imageAspectRatio) {
        Intent intent = new Intent(activity, PostDetailsActivity.class);
        intent.putExtra(POST_TO_SHOW_TAG, postToShow);
        intent.putExtra(IMAGE_RATIO_TAG, imageAspectRatio);

        //noinspection unchecked
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        new Pair<>(
                                description,
                                activity.getString(R.string.transition_name_description)),
                        new Pair<>(
                                author,
                                activity.getString(R.string.transition_name_author)),
                        new Pair<>(
                                rating,
                                activity.getString(R.string.transition_name_rating))/*,
                        new Pair<>(
                                image,
                                activity.getString(R.string.transition_name_image))*/
                );
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        ListView postDetailsListView = (ListView) findViewById(R.id.list_view_post_details);
        View mHeaderView = LayoutInflater.from(this).inflate(R.layout.header_view_post_details, null);
        postDetailsListView.addHeaderView(mHeaderView, null, false);
        mCommentsAdapter = new CommentsAdapter(this);
        postDetailsListView.setAdapter(mCommentsAdapter);

        ButterKnife.bind(this);

        Icepick.restoreInstanceState(this, savedInstanceState);
        mUiHandler = new Handler();

        mLoadResultCallback = new LoadGifImageReactor.LoadResultCallback() {
            @Override
            public void onLoadSuccessful() {
            }

            @Override
            public void onLoadFailed(@Nullable Exception exception) {
                failToLoadView.setVisibility(View.VISIBLE);
                playPause.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
        mTitle.setText(R.string.details_activity_name);
        imageToolbarView.setVisibility(View.GONE);

        float aspectRatio = getIntent().getFloatExtra(IMAGE_RATIO_TAG, 1);
        imageContainer.setAspectRatio(aspectRatio);

        int verticalMargin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        int maxHeight = DeviceUtils.getDeviceHeight(this) - verticalMargin * 2
                - toolbarHeight - DeviceUtils.getStatusBarHeight(this);
        imageContainer.setMaxHeight(maxHeight);

        Log.d(TAG, "onCreate(), mPost = " + mPost);
        if (mPost == null) {
            mPost = (Post) getIntent().getSerializableExtra(POST_TO_SHOW_TAG);
        }

        Intent intent = getIntent();
        if (mPost != null) {
            showCurrentPost();

        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            parseIntent(intent);

        } else {
            Log.e(TAG, "onCreate(), nothing to show in Details screen");
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.activity_details_nothing_to_show)
                    .setNegativeButton(R.string.dialog_close, (dialog, which) -> {onBackPressed();})
                    .create()
                    .show();
        }
    }

    private void parseIntent(Intent intent) {
        Uri data = intent.getData();
        String path = data.getPath().length() > 0 ? data.getPath().substring(1) : "";
        Long postId = null;

        try {
            postId = Long.valueOf(path);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse number from path", e);
        }

        Log.d(TAG, "data = " + data + ", path = " + path + ", postId = " + postId);

        if (postId != null) {
            loadPost(postId);

        } else {
            IntentHelper.sendIntentWithoutApp(this, intent, R.string.activity_details_cannot_show_link);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.details_menu_share_post_link:
                sharePostLink();
                break;
            case R.id.details_menu_share_post:
                sharePost();
                break;
            case R.id.details_menu_open_in_browser:
                openInBrowser();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @OnClick(R.id.btn_share_image_link)
    protected void shareImageLink() {
        PostViewHelper.shareImageLink(this, mPost.getGifURL());
    }

    @OnClick(R.id.btn_share_image)
    protected void shareImage() {
        PostViewHelper.shareImage(this, mPost.getGifURL());
    }

    @OnClick(R.id.btn_save_image)
    protected void saveImage() {
        PostViewHelper.saveImage(this, mPost);
    }

    @OnLongClick(R.id.btn_share_image_link)
    boolean showShareImageLinkHint() {
        return PostViewHelper.showShareImageLinkHint(this);
    }

    @OnLongClick(R.id.btn_share_image)
    boolean showShareImageHint() {
        return PostViewHelper.showShareImageHint(this);
    }

    @OnLongClick(R.id.btn_save_image)
    boolean showSaveImageHint() {
        return PostViewHelper.showSaveImageHint(this);
    }

    private void sharePost() {
        PostViewHelper.sharePost(this, mPost);
    }

    private void sharePostLink() {
        PostViewHelper.sharePostLink(this, mPost.getPostId());
    }

    private void openInBrowser() {
        if (!IntentHelper.openPostWebLinkExcludeSelf(this, mPost.getPostId())) {
            IntentHelper.onNoActivityFoundToHandleIntent(this, R.string.no_browser_found);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeSubscription.unsubscribe();

        if (mFutureRef.get() != null) {
            mFutureRef.get().cancel();
        }
    }

    private void loadPost(long postId) {
        Log.d(TAG, "loadPost(), postId = " + postId);

        Observable<Post> postsObservable = mApi.getPostById(postId);
        Subscription sub = postsObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onPostLoaded);
        mCompositeSubscription.add(sub);

        Observable<CommentsListHolder> postCommentsObservable = mApi.getCommentsByPostId(postId);
        Subscription sub2 = postCommentsObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCommentsLoaded);
        mCompositeSubscription.add(sub2);
    }

    private void onPostLoaded(Post post) {
        mPost = post;
        showCurrentPost();
    }

    private void onCommentsLoaded(CommentsListHolder commentsListHolder) {
        Log.d("test2", commentsListHolder.toString());
        mCommentsAdapter.setData(commentsListHolder.getComments());
        mCommentsAdapter.notifyDataSetChanged();
    }

    private void showCurrentPost() {
        Log.d(TAG, "showCurrentPost(), post = " + mPost);

        PostViewHelper.initCommonViews(descriptionView, authorView, ratingView, mPost);

        Observable<CommentsListHolder> postCommentsObservable = mApi.getCommentsByPostId(mPost.getPostId());
        Subscription sub2 = postCommentsObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCommentsLoaded);
        mCompositeSubscription.add(sub2);

        playPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                progressBar.setVisibility(View.VISIBLE);

            }
        });

        Future future = PostViewHelper.loadGifAsync(
                gifImageView,
                playPause,
                progressBar,
                null,
                mPost.getGifURL(),
                mUiHandler,
                this::onAspectRatioDetected,
                mLoadResultCallback,
                true);

        mFutureRef = new WeakReference<>(future);
    }

    private void onAspectRatioDetected(float aspectRatio) {
        if (Math.abs(imageContainer.getAspectRatio() - aspectRatio) > 0.01) {
            imageContainer.setAspectRatio(aspectRatio);
            imageContainer.invalidate();
        }
    }
}
