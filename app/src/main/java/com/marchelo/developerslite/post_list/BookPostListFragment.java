package com.marchelo.developerslite.post_list;

import android.content.Context;
import android.os.Bundle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.utils.SmartLoader;
import com.marchelo.developerslite.view.DividerItemDecorator;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public class BookPostListFragment extends APostListFragment implements LoaderManager.LoaderCallbacks<List<Post>> {

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
        mPostListView.setAdapter(new BookmarkPostListAdapter(getActivity()));
        mPostListView.addOnScrollListener(new QuickReturnScrollListener(mListener));
        int dividerHeight = getResources().getDimensionPixelSize(R.dimen.post_list_divider_height);
        mPostListView.addItemDecoration(new DividerItemDecorator(dividerHeight));
        mPostListView.setEmptyView(mEmptyView);
        mPostListView.setHasFixedSize(false);

        int end = getResources().getDimensionPixelSize(R.dimen.refresh_layout_progress_view_end);
        mSwipeLayout.setProgressViewEndTarget(false, end);
        mSwipeLayout.setOnRefreshListener(() -> getLoaderManager().getLoader(GetPostsLoader.ID).onContentChanged());
        mSwipeLayout.setRefreshing(false);

        getLoaderManager().initLoader(GetPostsLoader.ID, null, this);
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
        return new GetPostsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> data) {
        BookmarkPostListAdapter adapter = (BookmarkPostListAdapter) mPostListView.getAdapter();
        adapter.setData(data);
        adapter.notifyDataSetChanged();

        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<Post>> loader) {
        //do nothing
    }

    ///loader
    private static class GetPostsLoader extends SmartLoader<List<Post>> {
        public static final int ID = 555;

        public GetPostsLoader(Context context) {
            super(context);
        }

        @Override
        public List<Post> loadInBackground() {
            try {
                return DbHelper.from(getContext()).getAllPosts();
            } catch (SQLException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
    }
}