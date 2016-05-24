package com.marchelo.developerslite.details;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.marchelo.developerslite.BuildConfig;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Comment;
import com.marchelo.developerslite.utils.Config;
import com.marchelo.developerslite.utils.HtmlImproveHelper;
import com.marchelo.developerslite.utils.LinkifyModified;
import com.marchelo.developerslite.utils.StorageUtils;

import java.text.DateFormat;
import java.util.List;

/**
 * @author Oleg Green
 * @since 15.05.16
 */
public class CommentsAdapter extends BaseAdapter {
    public final DateFormat DATE_TIME_FORMATTER = Config.getDateFormat();
    private final CompoundButton mCommentsHeaderView;
    private final int mCommentResponseShiftPixels;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    private CommentsAdapterList mData = new CommentsAdapterList();
    private int mVeryLightGrayColor;
    private int mGrayColor;
    private int mColorPrimaryPale;
    private int mBlackColor;

    public CommentsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mCommentResponseShiftPixels = context.getResources().getDimensionPixelSize(R.dimen.comments_list_response_shift);
        mCommentsHeaderView = (CompoundButton) mLayoutInflater.inflate(R.layout.header_comments_view, null);
        mCommentsHeaderView.setChecked(StorageUtils.isExpandCommentsEnabled(context));
        mCommentsHeaderView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StorageUtils.setExpandCommentsEnabled(context, isChecked);
            notifyDataSetChanged();
        });

        mVeryLightGrayColor = mContext.getResources().getColor(R.color.very_light_gray_color);
        mColorPrimaryPale = mContext.getResources().getColor(R.color.colorPrimaryPale);
        mBlackColor = mContext.getResources().getColor(android.R.color.black);
        mGrayColor = mContext.getResources().getColor(R.color.gray_color);
    }

    public void setData(List<Comment> data) {
        mData = CommentsAdapterList.from(data);
        int size = mData.getItems().size();
        if (size > 0) {
            mCommentsHeaderView.setText(mContext.getResources().getQuantityString(R.plurals.details_n_comments_text, size, size));
            mCommentsHeaderView.setEnabled(true);
            mCommentsHeaderView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_comments_expand_selector, 0,
                    R.drawable.ic_comments_expand_selector, 0);

        } else {
            mCommentsHeaderView.setText(R.string.details_no_comments_text);
            mCommentsHeaderView.setEnabled(false);
            mCommentsHeaderView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    @Override
    public int getCount() {
        return mCommentsHeaderView.isChecked() ? (mData.getItems().size() + 1) : 1;
    }

    @Override
    public CommentsAdapterListItem getItem(int position) {
        return mData.getItems().get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            return mCommentsHeaderView;
        }

        CommentsAdapterListItem adapterItem = getItem(position);
        Comment comment = adapterItem.comment;

        View commentView;
        if (convertView == null) {
            commentView = mLayoutInflater.inflate(R.layout.item_view_comment, parent, false);
        } else {
            commentView = convertView;
        }

        TextView commentTextView = (TextView) commentView.findViewById(R.id.txt_comment);
        TextView authorTextView = (TextView) commentView.findViewById(R.id.txt_author);
        TextView dateTextView = (TextView) commentView.findViewById(R.id.txt_date);
        TextView ratingTextView = (TextView) commentView.findViewById(R.id.txt_rating);
        View layoutBgView = commentView.findViewById(R.id.layout_bg);
        View topDivider = commentView.findViewById(R.id.top_divider);

        if (comment.getVoteCount() >= 0) {
            layoutBgView.setBackgroundResource(R.drawable.comment_positive_bg);
            topDivider.setBackgroundColor(mColorPrimaryPale);
            commentTextView.setTextColor(mBlackColor);
            authorTextView.setTextColor(mGrayColor);
            dateTextView.setTextColor(mGrayColor);

        } else {
            layoutBgView.setBackgroundResource(R.drawable.comment_negative_bg);
            topDivider.setBackgroundColor(mVeryLightGrayColor);
            commentTextView.setTextColor(mGrayColor);
            authorTextView.setTextColor(mVeryLightGrayColor);
            dateTextView.setTextColor(mVeryLightGrayColor);
        }

        authorTextView.setText(comment.getAuthorName());
        dateTextView.setText(DATE_TIME_FORMATTER.format(comment.getDate()));
        ratingTextView.setText(getPreparedRatingString(comment));
        initTextViewWithComment(comment, commentTextView);

        commentView.setPadding(
                Math.min(adapterItem.itemDepth * mCommentResponseShiftPixels, Math.round(parent.getWidth() * 0.3f)),
                commentView.getPaddingTop(),
                commentView.getPaddingRight(),
                commentView.getPaddingBottom());

        //////////// debug start ////////////
        if (BuildConfig.DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            if (comment.getParentId() != 0) {
                builder.append(comment.getParentId());
                builder.append(":");
            }
            builder.append(comment.getId());
            builder.append(") ");
            builder.append(ratingTextView.getText());
            ratingTextView.setText(builder.toString());
        }
        //////////// debug end ////////////

        return commentView;
    }

    @NonNull
    private String getPreparedRatingString(Comment comment) {
        int voteCount = comment.getVoteCount();
        if (voteCount < 0) {
            return String.valueOf(voteCount);
        }
        return "+" + voteCount;
    }

    private void initTextViewWithComment(Comment comment, TextView textView) {
        String commentText = comment.getText();
        if (TextUtils.isEmpty(commentText)) {
            textView.setText("");

        } else {
            Spanned spanned = Html.fromHtml(commentText, null, null);
            textView.setText(HtmlImproveHelper.replaceImageSpansWithPlainText(spanned));

            LinkifyModified.addLinks(textView);
        }
    }
}