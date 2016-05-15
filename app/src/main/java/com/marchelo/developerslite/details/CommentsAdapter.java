package com.marchelo.developerslite.details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.marchelo.developerslite.R;

/**
 * @author Oleg Green
 * @since 15.05.16
 */
public class CommentsAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;

    public CommentsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View commentView = mLayoutInflater.inflate(R.layout.item_view_comment, parent, false);
        TextView textView = (TextView) commentView.findViewById(R.id.txt_comment);
        textView.setText("Some long long\n user comment");
        return commentView;
    }
}
