package com.marchelo.developerslite2.post_list;

import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.marchelo.developerslite2.R;
import com.marchelo.developerslite2.db.DbHelper;
import com.marchelo.developerslite2.model.Post;

import java.util.ArrayList;
import java.util.List;

public class BookmarkPostListAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private final LayoutInflater mInflater;
    private final Handler mUiHandler;
    private final DbHelper mDbHelper;
    private final List<Post> mItems = new ArrayList<>();

    public BookmarkPostListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mUiHandler = new Handler();
        mDbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
    }

    public void setData(List<Post> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostViewHolder(mInflater.inflate(R.layout.item_view_post, parent, false), mUiHandler, mDbHelper);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        holder.bindData(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}