package com.marchelo.developerslite.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Oleg Green
 * @since 15.05.16
 */
public class CommentsAdapter extends RecyclerView.Adapter {
    public final DateFormat DATE_TIME_FORMATTER = Config.getDateFormat();
    private final CompoundButton mCommentsHeaderView;
    private final int mCommentResponseShiftPixels;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final PostDetailsViewHolder mDetailsViewHolder;

    private CommentsAdapterList mAdapterList = new CommentsAdapterList();
    private int mVeryLightGrayColor;
    private int mGrayColor;
    private int mColorPrimaryPale;
    private int mBlackColor;

    @SuppressWarnings("deprecation")
    public CommentsAdapter(Context context, PostDetailsViewHolder detailsViewHolder) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mCommentResponseShiftPixels = context.getResources().getDimensionPixelSize(R.dimen.comments_list_response_shift);
        mDetailsViewHolder = detailsViewHolder;
        mCommentsHeaderView = (CompoundButton) detailsViewHolder.itemView.findViewById(R.id.header_comments_view);
        mCommentsHeaderView.setChecked(StorageUtils.isExpandCommentsEnabled(context));
        mCommentsHeaderView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StorageUtils.setExpandCommentsEnabled(context, isChecked);

            int commentsCount = mAdapterList.getItems().size();
            if (commentsCount > 1) {
                if (isChecked) {
                    notifyItemRangeInserted(1, commentsCount);
                } else {
                    notifyItemRangeRemoved(1, commentsCount);
                }
            }
        });

        mVeryLightGrayColor = mContext.getResources().getColor(R.color.very_light_gray_color);
        mColorPrimaryPale = mContext.getResources().getColor(R.color.colorPrimaryPale);
        mBlackColor = mContext.getResources().getColor(android.R.color.black);
        mGrayColor = mContext.getResources().getColor(R.color.gray_color);
    }

    public void setData(List<Comment> data) {
        mAdapterList = CommentsAdapterList.from(data);
        int size = mAdapterList.getItems().size();
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
    public int getItemCount() {
        return mCommentsHeaderView.isChecked() ? (mAdapterList.getItems().size() + 1) : 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return mDetailsViewHolder;
        }
        return new CommentViewHolder(parent, mLayoutInflater.inflate(R.layout.item_view_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).populate(mAdapterList.getItems().get(position - 1));
        } else if (!(holder instanceof PostDetailsViewHolder)) {
            throw new IllegalStateException("Unknown view holder had come: " + holder.getClass().getSimpleName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ViewGroup parent;

        @BindView(R.id.txt_comment)
        TextView commentTextView;

        @BindView(R.id.txt_author)
        TextView authorTextView;

        @BindView(R.id.txt_date)
        TextView dateTextView;

        @BindView(R.id.txt_rating)
        TextView ratingTextView;

        @BindView(R.id.layout_bg)
        View layoutBgView;

        @BindView(R.id.top_divider)
        View topDivider;

        @BindView(R.id.btn_show_hide_responses)
        CompoundButton showHideResponsesBtn;

//        @BindView(R.id.txt_responses_count)
//        TextView responsesCountView;

        @BindView(R.id.bottom_divider)
        View bottomDivider;

        public CommentViewHolder(ViewGroup parent, View itemView) {
            super(itemView);
            this.parent = parent;

            ButterKnife.bind(this, itemView);
        }

        public void populate(CommentsAdapterListItem adapterItem) {
            Comment comment = adapterItem.comment;

            if (comment.getVoteCount() >= 0) {
                layoutBgView.setBackgroundResource(R.drawable.comment_positive_bg);
                topDivider.setBackgroundColor(mColorPrimaryPale);
                commentTextView.setTextColor(mBlackColor);
                authorTextView.setTextColor(mGrayColor);
                dateTextView.setTextColor(mGrayColor);
                bottomDivider.setBackgroundColor(mColorPrimaryPale);
//                  responsesCountView.setTextColor(mGrayColor);
                showHideResponsesBtn.setTextColor(mGrayColor);

            } else {
                layoutBgView.setBackgroundResource(R.drawable.comment_negative_bg);
                topDivider.setBackgroundColor(mVeryLightGrayColor);
                commentTextView.setTextColor(mGrayColor);
                authorTextView.setTextColor(mVeryLightGrayColor);
                dateTextView.setTextColor(mVeryLightGrayColor);
                bottomDivider.setBackgroundColor(mVeryLightGrayColor);
//                  responsesCountView.setTextColor(mVeryLightGrayColor);
                showHideResponsesBtn.setTextColor(mGrayColor);
            }

            authorTextView.setText(comment.getAuthorName());
            dateTextView.setText(DATE_TIME_FORMATTER.format(comment.getDate()));
            ratingTextView.setText(getPreparedRatingString(comment));
            initTextViewWithComment(comment, commentTextView);
            showHideResponsesBtn.setOnCheckedChangeListener(null);

            if (adapterItem.getChildren().isEmpty()) {
                bottomDivider.setVisibility(View.GONE);
//                  responsesCountView.setVisibility(View.GONE);
                showHideResponsesBtn.setVisibility(View.GONE);

            } else {
                bottomDivider.setVisibility(View.VISIBLE);
//                  responsesCountView.setVisibility(View.VISIBLE);
                showHideResponsesBtn.setVisibility(View.VISIBLE);
                showHideResponsesBtn.setChecked(adapterItem.isChildrenVisible());
                showHideResponsesBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    adapterItem.setChildrenVisible(isChecked);
                    mAdapterList.rebuildList();
                    notifyDataSetChanged();
                });
            }

            itemView.setPadding(
                    Math.min(adapterItem.itemDepth * mCommentResponseShiftPixels, Math.round(parent.getWidth() * 0.3f)),
                    itemView.getPaddingTop(),
                    itemView.getPaddingRight(),
                    itemView.getPaddingBottom());

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
}