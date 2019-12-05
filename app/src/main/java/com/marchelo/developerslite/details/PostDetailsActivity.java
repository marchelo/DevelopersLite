package com.marchelo.developerslite.details;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.marchelo.developerslite.BuildConfig;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.network.ApiFactory;
import com.marchelo.developerslite.utils.IntentHelper;
import com.marchelo.developerslite.view.DividerItemDecorator;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
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
    private static final String IMAGE_RATIO_TAG = "IMAGE_RATIO";

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private final ApiFactory.ApiPostById mApi = ApiFactory.postByIdApi();

    private PostDetailsViewHolder mHeaderViewHolder;
    private CommentsAdapter mCommentsAdapter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mTitle;

    @BindView(R.id.list_view_post_details)
    RecyclerView mPostDetailsListView;

    @State Post mPost;

    public static void startActivity(Activity activity, Post postToShow, View description,
                                     View author, View rating, float imageAspectRatio) {
        Intent intent = new Intent(activity, PostDetailsActivity.class);
        intent.putExtra(POST_TO_SHOW_TAG, postToShow);
        intent.putExtra(IMAGE_RATIO_TAG, imageAspectRatio);

        //noinspection unchecked,unused
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
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
//        ActivityCompat.startActivity(activity, intent, options.toBundle());
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);

        Icepick.restoreInstanceState(this, savedInstanceState);

        mPostDetailsListView.setLayoutManager(new LinearLayoutManager(this));
        mPostDetailsListView.setHasFixedSize(false);
        int dividerHeight = getResources().getDimensionPixelSize(R.dimen.detailed_list_dividerHeight);
        mPostDetailsListView.addItemDecoration(new DividerItemDecorator(dividerHeight));

        View headerView = LayoutInflater.from(this).inflate(R.layout.header_view_post_details, mPostDetailsListView, false);
        mHeaderViewHolder = new PostDetailsViewHolder(headerView, new Handler(), getIntent().getFloatExtra(IMAGE_RATIO_TAG, 1));
        mCommentsAdapter = new CommentsAdapter(this, mHeaderViewHolder);
        mPostDetailsListView.setAdapter(mCommentsAdapter);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
        mTitle.setText(R.string.details_activity_name);

        init();
    }

    private void init() {
        Log.d(TAG, "init(), mPost = " + mPost);
        if (mPost == null) {
            mPost = (Post) getIntent().getSerializableExtra(POST_TO_SHOW_TAG);
        }

        if (mPost != null) {
            showLoadedPost();

        } else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            parseIntent(getIntent());

        } else {
            Log.e(TAG, "onCreate(), nothing to show in Details screen");
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.activity_details_nothing_to_show)
                    .setNegativeButton(R.string.dialog_close, (dialog, which) -> onBackPressed())
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
            IntentHelper.sendIntentExcludingThisApp(this, intent, R.string.activity_details_cannot_show_link);
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
                mHeaderViewHolder.sharePostLink();
                break;

            case R.id.details_menu_share_post:
                mHeaderViewHolder.sharePost();
                break;

            case R.id.details_menu_open_in_browser:
                mHeaderViewHolder.openInBrowser();
                break;

            case R.id.details_menu_bookmark_post:
//                if (item.isChecked()) {
//                } else {
//                }
                item.setChecked(!item.isChecked());
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        mHeaderViewHolder.release();
    }

    private void loadPost(long postId) {
        Log.d(TAG, "loadPost(), postId = " + postId);
        mCompositeSubscription.add(
                mApi.getPostById(postId)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((post) -> {
                            mPost = post;
                            showLoadedPost();
                        }));
    }

    private void showLoadedPost() {
        Log.d(TAG, "showLoadedPost(), post = " + mPost);

        mCompositeSubscription.add(
                mApi.getCommentsByPostId(mPost.getPostId())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((commentsListHolder) -> {
                            mCommentsAdapter.setData(commentsListHolder.getComments());
                            mCommentsAdapter.notifyDataSetChanged();
                        }));

        mHeaderViewHolder.loadPostImage(mPost);

        if (BuildConfig.DEBUG) {
            appendPostIdToTitle();
        }
    }

    @SuppressLint("SetTextI18n")
    private void appendPostIdToTitle() {
        mTitle.setText(mTitle.getText() + "(" + mPost.getPostId() + ")");
    }
}