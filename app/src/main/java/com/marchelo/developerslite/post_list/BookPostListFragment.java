package com.marchelo.developerslite.post_list;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.view.DividerItemDecorator;

import java.sql.SQLException;

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public class BookPostListFragment extends APostListFragment {

    protected BookmarkPostListAdapter mPostsAdapter;

    public static BookPostListFragment newInstance(String title) {
        Bundle args = new Bundle();
        args.putString(TITLE_TAG, title);
        BookPostListFragment fragment = new BookPostListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        mPostListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostListView.setHasFixedSize(false);

        DbHelper databaseHelper
                = OpenHelperManager.getHelper(getActivity(), DbHelper.class);

        initAdapterWithNewDataCursor(databaseHelper);

        mPostListView.addOnScrollListener(new QuickReturnScrollListener(mListener));
        mPostListView.setEmptyView(mEmptyView);

        int dividerHeight = getResources().getDimensionPixelSize(R.dimen.post_list_divider_height);
        mPostListView.addItemDecoration(new DividerItemDecorator(dividerHeight));

        int end = getResources().getDimensionPixelSize(R.dimen.refresh_layout_progress_view_end);
        mSwipeLayout.setProgressViewEndTarget(false, end);
        mSwipeLayout.setOnRefreshListener(() -> initAdapterWithNewDataCursor(databaseHelper));

        /*if (savedInstanceState == null) {
            //TODO load new data from db
            mSwipeLayout.setRefreshing(true);
        }*//* else {
            mPostsAdapter.notifyDataSetChanged();
        }*/
    }

    private void initAdapterWithNewDataCursor(DbHelper databaseHelper) {

        CloseableIterator<Post> iterator = null;
        try {
            Dao<Post, Long> postDao = databaseHelper.getPostDao();
            QueryBuilder<Post, Long> qb = postDao.queryBuilder();
            iterator = postDao.iterator(qb.orderBy(Post.Column.ID, false).prepare());
            AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
            Cursor cursor = results.getRawCursor();
            mPostsAdapter = new BookmarkPostListAdapter(getActivity(), cursor);

            mPostListView.setAdapter(mPostsAdapter);
            mSwipeLayout.setRefreshing(false);

        } catch (SQLException e) {
            e.printStackTrace();
            //noinspection ConstantConditions
            if (iterator != null) {
                iterator.closeQuietly();
            }
        }
    }
}
