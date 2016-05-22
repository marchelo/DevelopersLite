package com.marchelo.developerslite.details;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Comment;

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

        Spanned spanned = Html.fromHtml(comment.getText(), null, null);
        textView.setText(doMagic(spanned));

        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setAutoLinkMask(Linkify.WEB_URLS);


        return commentView;
    }

    private Spanned doMagic(Spanned spanned) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        ImageSpan[] imageSpans = builder.getSpans(0, builder.length(), ImageSpan.class);
        for (ImageSpan imageSpan : imageSpans) {
            int spanStart = builder.getSpanStart(imageSpan);
            int spanEnd = builder.getSpanEnd(imageSpan);
//            Uri imageUri = Uri.parse(imageSpan.getSource());


            builder.replace(spanStart, spanEnd, imageSpan.getSource());


            builder.removeSpan(imageSpan);
        }
        return builder;
    }
}
