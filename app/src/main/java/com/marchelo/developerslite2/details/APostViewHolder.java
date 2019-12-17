package com.marchelo.developerslite2.details;

import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.marchelo.developerslite2.R;
import com.marchelo.developerslite2.R2;
import com.marchelo.developerslite2.model.Post;
import com.marchelo.developerslite2.post_list.GifImageHolder;
import com.marchelo.developerslite2.utils.IntentHelper;
import com.marchelo.developerslite2.utils.PostViewHelper;
import com.marchelo.developerslite2.view.ExpandableTextView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author Oleg Green
 * @since 26.05.16
 */
public class APostViewHolder extends GifImageHolder {

    @BindView(R2.id.tv_description)
    protected ExpandableTextView descriptionView;

    @BindView(R2.id.tv_author)
    protected TextView authorView;

    @BindView(R2.id.tv_rating)
    protected TextView ratingView;

    @BindView(R2.id.tv_date)
    protected TextView dateView;

    protected Post mPost;

    public APostViewHolder(View itemView, Handler handler) {
        super(itemView, handler);
    }

    @Override
    @NonNull
    protected String getGifNameForLocalSave() {
        return String.valueOf(mPost.getPostId());
    }

    @Optional @OnClick(R2.id.btn_share_post)
    public void sharePost() {
        PostViewHelper.sharePost(mContext, mPost);
    }

    @Optional @OnClick(R2.id.btn_share_post_link)
    public void sharePostLink() {
        PostViewHelper.sharePostLink(mContext, mPost.getPostId());
    }

    public void openInBrowser() {
        if (!IntentHelper.openPostWebLinkExcludeSelf(mContext, mPost.getPostId())) {
            IntentHelper.onNoActivityFoundToHandleIntent(mContext, R.string.no_browser_found);
        }
    }
}