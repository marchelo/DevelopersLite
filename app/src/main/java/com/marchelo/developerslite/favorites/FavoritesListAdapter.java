package com.marchelo.developerslite.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Favorite;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Picasso mPicasso;
    private final List<Favorite> mItems = new ArrayList<>();

    public FavoritesListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mPicasso = new Picasso.Builder(context).build();
    }

    public void setData(List<Favorite> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Favorite getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Favorite favorite = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_view_favorite, parent, false);
        }

        ImageView itemImageView = (ImageView) convertView.findViewById(R.id.image_preview);
        itemImageView.setImageResource(R.drawable.favorite_initial_image);
        mPicasso.load(favorite.getPreviewURL())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.animated_favorite_place_holder)
                .error(R.drawable.favorite_load_fail_image)
                .into(itemImageView);

        return convertView;
    }
}