package com.marchelo.developerslite.details;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Comment;
import com.marchelo.developerslite.utils.HtmlImproveHelper;
import com.marchelo.developerslite.utils.LinkifyModified;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Green
 * @since 15.05.16
 */
public class CommentsAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private List<Comment> mData = new ArrayList<>(0);

    public CommentsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<Comment> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Comment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment comment = getItem(position);

        View commentView = mLayoutInflater.inflate(R.layout.item_view_comment, parent, false);
        TextView textView = (TextView) commentView.findViewById(R.id.txt_comment);

        initTextViewWithComment(comment, textView);

        return commentView;
    }

    private void initTextViewWithComment(Comment comment, TextView textView) {
        Spanned spanned = Html.fromHtml(comment.getText(), null, null);
        textView.setText(HtmlImproveHelper.replaceImageSpansWithPlainText(spanned));

        LinkifyModified.addLinks(textView);
    }
}