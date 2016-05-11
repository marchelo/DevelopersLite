package com.marchelo.developerslite.post_list;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.koushikdutta.async.future.Future;
import com.marchelo.developerslite.DevLifeApplication;
import com.marchelo.developerslite.PostDetailsActivity;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Favorite;
import com.marchelo.developerslite.utils.ViewsTintConfig;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.utils.LoadGifImageReactor;
import com.marchelo.developerslite.utils.PostViewHelper;
import com.marchelo.developerslite.view.ExpandableTextView;
import com.marchelo.developerslite.view.ImageShareToolbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.text.DateFormat;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import pl.droidsonroids.gif.GifImageButton;

/**
 * @author Oleg Green
 * @since 17.09.15
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    private static final int GIF_FUTURE_KEY = R.id.gif_future_key;
    private static final int IMAGE_TARGET_KEY = R.id.image_target_key;

    private final DbHelper mDbHelper;
    private final Handler mUiHandler;
    private final Context mContext;

    @Bind(R.id.gif_image)           GifImageButton gifImageView;
    @Bind(R.id.tv_description)      ExpandableTextView descriptionView;
    @Bind(R.id.tv_author)           TextView authorView;
    @Bind(R.id.tv_rating)           TextView ratingView;
    @Bind(R.id.tv_date)             TextView dateView;
    @Bind(R.id.btn_play_pause)      CompoundButton playPause;
    @Bind(R.id.progress_bar)        ProgressBar progressBar;
    @Bind(R.id.btn_image_toolbar)   ImageShareToolbar imageToolbarView;
    @Bind(R.id.view_load_fail)      View failToLoadView;
    @Bind(R.id.btn_bookmark)        CompoundButton bookmarkBtn;
    @Bind(R.id.btn_save_gif_link)   ImageButton mSaveLinkBtn;
    @Bind(R.id.btn_share_post)      ImageButton mSharePostBtn;
    @Bind(R.id.btn_share_post_link) ImageButton mSharePostLinkBtn;
    @Bind(R.id.btn_details)         ImageButton mDetailsBtn;

    @BindColor(R.color.colorPrimary) int mPrimaryColorValue;

    private Post mPost;

    public PostViewHolder(View itemView, Handler uiHandler, DbHelper dbHelper) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mDbHelper = dbHelper;
        mContext = itemView.getContext();
        mUiHandler = uiHandler;

        bookmarkBtn.setButtonDrawable(ViewsTintConfig.getTinted(mContext,
                R.drawable.ic_bookmark_selector, R.color.bookmark_button_tint_selector));

        mSaveLinkBtn.setImageDrawable(ViewsTintConfig.getTinted(mContext,
                R.drawable.ic_favorite_white_selector, R.color.bookmark_button_tint_selector));

        mSharePostBtn.setColorFilter(mPrimaryColorValue);
        mSharePostLinkBtn.setColorFilter(mPrimaryColorValue);
        mDetailsBtn.setColorFilter(mPrimaryColorValue);
    }

    public void bindData(Post post) {
        final boolean autoLoadGifs = DevLifeApplication.getInstance().isAutoLoadGifs();
        Picasso picasso = Picasso.with(mContext);
        mPost = post;

        bookmarkBtn.setOnCheckedChangeListener(null);
        try {
            Dao<Post, Long> postDao = mDbHelper.getPostDao();
            bookmarkBtn.setChecked(!postDao.queryForEq(Post.Column.POST_ID, mPost.getPostId()).isEmpty());
            bookmarkBtn.setOnCheckedChangeListener(new OnBookmarkedListener(postDao, mPost));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mSaveLinkBtn.setOnClickListener(null);
        try {
            mSaveLinkBtn.setSelected(mDbHelper.getFavoriteByPostId(mPost.getPostId()) != null);
            mSaveLinkBtn.setOnClickListener(new OnAddToFavoriteListener(mDbHelper, mPost));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        failToLoadView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        imageToolbarView.imageUnavailable();
        imageToolbarView.show();
        playPause.setVisibility(View.VISIBLE);
        playPause.setOnCheckedChangeListener(null);
        playPause.setChecked(false);
        playPause.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                if (gifImageView.getTag(GIF_FUTURE_KEY) == null) {
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

        //cancel gif loading task
        //noinspection unchecked
        WeakReference<Future> gifFuture = (WeakReference<Future>) gifImageView.getTag(GIF_FUTURE_KEY);
        gifImageView.setTag(GIF_FUTURE_KEY, null);
        if (gifFuture != null && gifFuture.get() != null && !gifFuture.get().isCancelled()) {
            gifFuture.get().cancel();
        }

        //cancel image preview loading task
        Target targetToCancel = (Target) gifImageView.getTag(IMAGE_TARGET_KEY);
        if (gifFuture != null) {
            picasso.cancelRequest(targetToCancel);
        }

        if (descriptionView.isDirty()) {
            descriptionView.resetState();
        }

        //initialize view with data
        PostViewHelper.initCommonViews(descriptionView, authorView, ratingView, post);
        dateView.setText(mContext.getString(R.string.post_item_date, DateFormat.getDateInstance().format(post.getDate())));

        //Load preview image
        LoadPreviewImageCallback target = new LoadPreviewImageCallback(this);
        gifImageView.setTag(IMAGE_TARGET_KEY, target);
        //TODO resize image to avoid using extra big sizes
        picasso.load(post.getPreviewURL())
                .into(target);

        if (autoLoadGifs) {
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
        gifImageView.setTag(GIF_FUTURE_KEY, new WeakReference<>(future));
    }

    private float getDrawableAspectRatio(Drawable drawable) {
        return (float) drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
    }

    @OnClick(R.id.btn_share_image_link)
    protected void shareImageLink() {
        PostViewHelper.shareImageLink(mContext, mPost.getGifURL());
    }

    @OnClick(R.id.btn_share_image)
    protected void shareImage() {
        PostViewHelper.shareImage(mContext, mPost.getGifURL());
    }

    @OnClick(R.id.btn_save_image)
    protected void saveImage() {
        PostViewHelper.saveImage(mContext, mPost);
    }

    @OnClick(R.id.btn_share_post)
    protected void sharePost() {
        PostViewHelper.sharePost(mContext, mPost);
    }

    @OnClick(R.id.btn_share_post_link)
    protected void sharePostLink() {
        PostViewHelper.sharePostLink(mContext, mPost.getPostId());
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

    @OnLongClick(R.id.btn_share_image_link)
    boolean showShareImageLinkHint() {
        return PostViewHelper.showShareImageLinkHint(mContext);
    }

    @OnLongClick(R.id.btn_share_image)
    boolean showShareImageHint() {
        return PostViewHelper.showShareImageHint(mContext);
    }

    @OnLongClick(R.id.btn_save_image)
    boolean showSaveImageHint() {
        return PostViewHelper.showSaveImageHint(mContext);
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

    private static class OnBookmarkedListener implements CompoundButton.OnCheckedChangeListener {

        private final Dao<Post, Long> mPostDao;
        private final Post mCurrentPost;

        public OnBookmarkedListener(Dao<Post, Long> postDao, Post post) {
            mPostDao = postDao;
            mCurrentPost = post;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {

                Post foundPost = mPostDao.queryBuilder()
                        .where().eq(Post.Column.POST_ID, mCurrentPost.getPostId())
                        .queryForFirst();

                if (isChecked && foundPost == null) {
                    mPostDao.create(mCurrentPost);

                } else if (foundPost != null) {
                    mPostDao.deleteById(foundPost.getId());
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static class OnAddToFavoriteListener implements View.OnClickListener {

        private final DbHelper mDbHelper;
        private final Post mCurrentPost;

        public OnAddToFavoriteListener(DbHelper dbHelper, Post post) {
            mDbHelper = dbHelper;
            mCurrentPost = post;
        }

        @Override
        public void onClick(View view) {
            try {
                Favorite foundFavorite = mDbHelper.getFavoriteByPostId(mCurrentPost.getPostId());

                if (!view.isSelected() && foundFavorite == null) {
                    mDbHelper.addFavorite(new Favorite(mCurrentPost.getPostId(), mCurrentPost.getGifURL(), mCurrentPost.getPreviewURL()));

                } else if (foundFavorite != null) {
                    mDbHelper.deleteFavorite(foundFavorite);
                }
                view.setSelected(!view.isSelected());

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}