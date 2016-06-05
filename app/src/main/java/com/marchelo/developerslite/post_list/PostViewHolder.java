package com.marchelo.developerslite.post_list;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.DevLifeApplication;
import com.marchelo.developerslite.details.APostViewHolder;
import com.marchelo.developerslite.details.PostDetailsActivity;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Favorite;
import com.marchelo.developerslite.utils.ViewsTintConfig;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DateFormat;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Oleg Green
 * @since 17.09.15
 */
public class PostViewHolder extends APostViewHolder {
    protected LoadPreviewImageCallback mLoadImageCallback;
    protected CompositeSubscription mSubscriptions;

    private final DbHelper mDbHelper;
    private Picasso mPicasso;

    @BindView(R.id.btn_bookmark)
    protected CompoundButton bookmarkBtn;

    @BindView(R.id.btn_share_post)
    protected ImageButton mSharePostBtn;

    @BindView(R.id.btn_share_post_link)
    protected ImageButton mSharePostLinkBtn;

    @BindView(R.id.btn_details)
    protected ImageButton mDetailsBtn;

    @BindColor(R.color.colorPrimary) int mPrimaryColorValue;

    public PostViewHolder(View itemView, Handler uiHandler, DbHelper dbHelper) {
        super(itemView, uiHandler);

        mDbHelper = dbHelper;
        mPicasso = Picasso.with(mContext);

        bookmarkBtn.setButtonDrawable(ViewsTintConfig.getTinted(mContext,
                R.drawable.ic_bookmark_selector, R.color.bookmark_button_tint_selector));

        mSharePostBtn.setColorFilter(mPrimaryColorValue);
        mSharePostLinkBtn.setColorFilter(mPrimaryColorValue);
        mDetailsBtn.setColorFilter(mPrimaryColorValue);

        itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (mSubscriptions != null) {
                    mSubscriptions.unsubscribe();
                    mSubscriptions = null;
                }
            }
        });
    }

    public void bindData(Post post) {
        mPost = post;
        mGifUriString = post.getGifURL();

        cancelGifRequestIfNeeded();
        recycleGifDrawableIfNeeded();
        cancelPreviewRequestIfNeeded();

        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
        }
        mSubscriptions = new CompositeSubscription();

        bookmarkBtn.setOnCheckedChangeListener(null);
        mSubscriptions.add(
                mDbHelper.getPostByPostIdAsync(mPost.getPostId())
                        .subscribe(
                                post1 -> {
                                    bookmarkBtn.setChecked(post1 != null);
                                    bookmarkBtn.setOnCheckedChangeListener(
                                            new OnBookmarkedListener(mSubscriptions, mDbHelper, mPost));
                                }));

        saveLinkGifView.setOnClickListener(null);
        mSubscriptions.add(
                mDbHelper.getFavoriteByPostIdAsync(mPost.getPostId())
                        .subscribe(
                                favorite -> {
                                    saveLinkGifView.setSelected(favorite != null);
                                    saveLinkGifView.setOnClickListener(
                                            new OnAddToFavoriteListener(mSubscriptions, mDbHelper, mPost));
                                }));

        failToLoadView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        imageToolbarView.imageUnavailable();
        imageToolbarView.show();
        playPause.setVisibility(View.VISIBLE);
        playPause.setOnCheckedChangeListener(null);
        playPause.setChecked(false);
        playPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                if (mFutureRef == null) {
                    startGifLoading(post, true);
                }

                progressBar.setVisibility(View.VISIBLE);
                imageToolbarView.hide();

            } else {
                imageToolbarView.show();
            }
        });
        gifImageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        gifImageView.setOnClickListener(null);

        if (descriptionView.isDirty()) {
            descriptionView.resetState();
        }

        //initialize view with data
        PostViewHelper.initCommonViews(descriptionView, authorView, ratingView, post);
        dateView.setText(mContext.getString(R.string.post_item_date, DateFormat.getDateInstance().format(post.getDate())));

        //Load preview image
        mLoadImageCallback = new LoadPreviewImageCallback(this);
        //TODO resize image to avoid using extra big sizes
        mPicasso.load(post.getPreviewURL())
                .into(mLoadImageCallback);

        mFutureRef = null;
        if (DevLifeApplication.getInstance().isAutoLoadGifs()) {
            startGifLoading(post, false);
        }
    }

    private void startGifLoading(Post post, boolean autoStart) {
        //Load Gif image
        Future future = PostViewHelper.loadGifAsync(
                gifImageView,
                playPause,
                progressBar,
                imageToolbarView,
                post.getGifURL(),
                mUiHandler,
                null,
                new LoadGifImageReactor.LoadResultCallback() {
                    @Override
                    public void onLoadSuccessful() {
                        imageToolbarView.imageAvailable();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Exception exception) {
                        //nothing to do
                    }
                },
                autoStart);
        mFutureRef = new WeakReference<>(future);
    }

    private float getDrawableAspectRatio(Drawable drawable) {
        return (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
    }

    @OnClick(R.id.btn_save_gif_link)
    protected void saveImageLink(View view) {
        view.setSelected(!view.isSelected());
    }

    @OnClick(R.id.btn_details)
    protected void showDetails() {
        PostDetailsActivity.startActivity(
                (Activity) mContext,
                mPost,
                descriptionView,
                authorView,
                ratingView,
                getDrawableAspectRatio(gifImageView.getDrawable()));
    }

    @OnLongClick(R.id.btn_share_post)
    boolean showSharePostHint() {
        Toast.makeText(mContext, R.string.btn_share_post_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    @OnLongClick(R.id.btn_share_post_link)
    boolean showSharePostLinkHint() {
        Toast.makeText(mContext, R.string.btn_share_post_link_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    @OnLongClick(R.id.btn_details)
    boolean showDetailsHint() {
        Toast.makeText(mContext, R.string.btn_details_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    @OnLongClick(R.id.btn_bookmark)
    boolean toggleBookmarkHint() {
        Toast.makeText(mContext, R.string.btn_bookmarks_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void cancelPreviewRequestIfNeeded() {
        if (mLoadImageCallback != null) {
            mPicasso.cancelRequest(mLoadImageCallback);
        }
    }



    ////////
    private static class OnBookmarkedListener implements CompoundButton.OnCheckedChangeListener {

        private final CompositeSubscription mGlobalSubscriptions;
        private final DbHelper mDbHelper;
        private final Post mCurrentPost;

//        private CompositeSubscription mLocalSubscriptions;

        public OnBookmarkedListener(CompositeSubscription subscriptions, DbHelper dbHelper, Post post) {
            mGlobalSubscriptions = subscriptions;
            mDbHelper = dbHelper;
            mCurrentPost = post;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            if (mLocalSubscriptions != null) {
//                mLocalSubscriptions.unsubscribe();
//            }
//            mLocalSubscriptions = new CompositeSubscription();

            if (isChecked) {
                Subscription addIfAbsentSubscription = mDbHelper.addPostIfAbsentAsync(mCurrentPost)
                        .subscribe(aBoolean -> {
                            Log.d("test2", "onCheckedChanged: addPostIfAbsentAsync: result = " + aBoolean);
                        });
                mGlobalSubscriptions.add(addIfAbsentSubscription);
//                mLocalSubscriptions.add(addIfAbsentSubscription);

            } else {
                Subscription deleteIfPresentSubscription = mDbHelper.deletePostIfPresentAsync(mCurrentPost)
                        .subscribe(aBoolean -> {
                            Log.d("test2", "onCheckedChanged: deletePostIfPresentAsync: result = " + aBoolean);
                        });
                mGlobalSubscriptions.add(deleteIfPresentSubscription);
//                mLocalSubscriptions.add(deleteIfPresentSubscription);
            }
        }
    }

    private static class OnAddToFavoriteListener implements View.OnClickListener {

        private final CompositeSubscription mGlobalSubscriptions;
        private final DbHelper mDbHelper;
        private final Post mCurrentPost;

//        private CompositeSubscription mLocalSubscriptions;

        public OnAddToFavoriteListener(CompositeSubscription subscriptions, DbHelper dbHelper, Post post) {
            mGlobalSubscriptions = subscriptions;
            mDbHelper = dbHelper;
            mCurrentPost = post;
        }

        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

//            if (mLocalSubscriptions != null) {
//                mLocalSubscriptions.unsubscribe();
//            }
//            mLocalSubscriptions = new CompositeSubscription();

            if (view.isSelected()) {
                Subscription addFavIfAbsent = mDbHelper.addFavoriteIfAbsentAsync(Favorite.createFrom(mCurrentPost))
                        .subscribe(aBoolean -> {
//                            Log.d("test2", "onClick: addFavoriteIfAbsentAsync: result = " + aBoolean);
                        });
                mGlobalSubscriptions.add(addFavIfAbsent);
//                mLocalSubscriptions.add(addFavIfAbsent);

            } else {
                Subscription deleteFavIfPresent = mDbHelper.deleteFavoriteIfPresentAsync(mCurrentPost.getPostId())
                        .subscribe(aBoolean -> {
//                            Log.d("test2", "onClick: deleteFavoriteIfPresentAsync: result = " + aBoolean);
                        });
                mGlobalSubscriptions.add(deleteFavIfPresent);
//                mLocalSubscriptions.add(deleteFavIfPresent);
            }
        }
    }
}