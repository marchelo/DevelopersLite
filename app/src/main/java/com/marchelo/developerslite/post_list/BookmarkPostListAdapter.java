package com.marchelo.developerslite.post_list;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.view.CursorRecyclerViewAdapter;

import java.util.Date;

public class BookmarkPostListAdapter extends CursorRecyclerViewAdapter<PostViewHolder> {
    private final LayoutInflater mInflater;
    private final Handler mUiHandler;
    private final DbHelper mDbHelper;

    public BookmarkPostListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mInflater = LayoutInflater.from(context);
        mUiHandler = new Handler();
        mDbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostViewHolder(mInflater.inflate(R.layout.item_view_post, parent, false), mUiHandler, mDbHelper);
    }

    @Override
    public void onBindViewHolder(PostViewHolder viewHolder, Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getLong(cursor.getColumnIndex(Post.Column.ID)));
        post.setPostId(cursor.getLong(cursor.getColumnIndex(Post.Column.POST_ID)));
        post.setDescription(cursor.getString(cursor.getColumnIndex(Post.Column.DESC)));
        post.setVotes(cursor.getInt(cursor.getColumnIndex(Post.Column.VOTES)));
        post.setAuthor(cursor.getString(cursor.getColumnIndex(Post.Column.AUTHOR)));
        post.setDate(new Date(cursor.getLong(cursor.getColumnIndex(Post.Column.DATE))));
        post.setPreviewURL(cursor.getString(cursor.getColumnIndex(Post.Column.PREVIEW_URL)));
        post.setGifURL(cursor.getString(cursor.getColumnIndex(Post.Column.GIF_URL)));
        post.setType(Post.Type.valueOf(cursor.getString(cursor.getColumnIndex(Post.Column.TYPE))));

        viewHolder.bindData(post);
    }
}