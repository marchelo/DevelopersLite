package com.marchelo.developerslite.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.marchelo.developerslite.BuildConfig;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Comment;
import com.marchelo.developerslite.utils.Config;
import com.marchelo.developerslite.utils.HtmlImproveHelper;
import com.marchelo.developerslite.utils.LinkifyModified;

import java.text.DateFormat;
import java.util.List;

/**
 * @author Oleg Green
 * @since 15.05.16
 */
public class CommentsAdapter extends BaseAdapter {
    public final DateFormat DATE_TIME_FORMATTER = Config.getDateFormat();
    private final LayoutInflater mLayoutInflater;

    private CommentsAdapterList mData = new CommentsAdapterList();
    private final int mCommentResponseShiftPixels;

    public CommentsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mCommentResponseShiftPixels = context.getResources().getDimensionPixelSize(R.dimen.comments_list_response_shift);
    }

    public void setData(List<Comment> data) {
        mData = CommentsAdapterList.from(data);
    }

    @Override
    public int getCount() {
        return mData.getItems().size();
    }

    @Override
    public CommentsAdapterListItem getItem(int position) {
        return mData.getItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentsAdapterListItem adapterItem = getItem(position);
        Comment comment = adapterItem.comment;

        View commentView = mLayoutInflater.inflate(R.layout.item_view_comment, parent, false);
        TextView commentTextView = (TextView) commentView.findViewById(R.id.txt_comment);
        TextView authorTextView = (TextView) commentView.findViewById(R.id.txt_author);
        TextView dateTextView = (TextView) commentView.findViewById(R.id.txt_date);
        TextView ratingTextView = (TextView) commentView.findViewById(R.id.txt_rating);
        View layoutBgView = commentView.findViewById(R.id.layout_bg);
        View topDivider = commentView.findViewById(R.id.top_divider);

        if (comment.getVoteCount() >= 0) {
            layoutBgView.setBackgroundResource(R.drawable.comment_positive_bg);
            topDivider.setBackgroundColor(parent.getResources().getColor(R.color.colorPrimaryPale));
            commentTextView.setTextColor(parent.getResources().getColor(android.R.color.black));
            authorTextView.setTextColor(parent.getResources().getColor(R.color.gray_color));
            dateTextView.setTextColor(parent.getResources().getColor(R.color.gray_color));
        } else {
            layoutBgView.setBackgroundResource(R.drawable.comment_negative_bg);
            topDivider.setBackgroundColor(parent.getResources().getColor(R.color.very_light_gray_color));
            commentTextView.setTextColor(parent.getResources().getColor(R.color.gray_color));
            authorTextView.setTextColor(parent.getResources().getColor(R.color.very_light_gray_color));
            dateTextView.setTextColor(parent.getResources().getColor(R.color.very_light_gray_color));
        }

        authorTextView.setText(comment.getAuthorName());
        dateTextView.setText(DATE_TIME_FORMATTER.format(comment.getDate()));
        ratingTextView.setText(getPreparedRatingString(comment));
        initTextViewWithComment(comment, commentTextView);

        commentView.setPadding(
                Math.min(adapterItem.itemDepth * mCommentResponseShiftPixels, Math.round(parent.getWidth() * 0.4f)),
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
        Spanned spanned = Html.fromHtml(comment.getText(), null, null);
        textView.setText(HtmlImproveHelper.replaceImageSpansWithPlainText(spanned));

        LinkifyModified.addLinks(textView);
    }
}