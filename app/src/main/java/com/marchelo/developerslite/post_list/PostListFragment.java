package com.marchelo.developerslite.post_list;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.network.ApiFactory;
import com.marchelo.developerslite.manager.PostLoadManager;
import com.marchelo.developerslite.view.DividerItemDecorator;

import java.util.List;

/**
 * @author Oleg Green
 * @since 16.08.15
 */
public class PostListFragment extends APostListFragment {
    private static final String TAG = PostListFragment.class.getSimpleName();
    protected static final String SERVICE_API_TAG = "SERVICE_API";

    protected PostLoadManager mPostLoadManager;
    protected PostListAdapter mPostsAdapter;

    public static PostListFragment newInstance(ApiFactory.ApiByPage api, String title) {
        Bundle args = new Bundle();
        args.putSerializable(SERVICE_API_TAG, api);
        args.putString(TITLE_TAG, title);
        PostListFragment postListFragment = new PostListFragment();
        postListFragment.setArguments(args);
        return postListFragment;
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        mPostListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostListView.setHasFixedSize(false);
        mPostsAdapter = new PostListAdapter(getActivity());
        mPostListView.setAdapter(mPostsAdapter);
        mPostListView.addOnScrollListener(new AutoLoadScrollListener());
        mPostListView.addOnScrollListener(new QuickReturnScrollListener(mListener));
        mPostListView.setEmptyView(mEmptyView);
        mPostsAdapter.setTryAgainListener(this::requestLoadMoreItems);

        int dividerHeight = getResources().getDimensionPixelSize(R.dimen.post_list_divider_height);
        mPostListView.addItemDecoration(new DividerItemDecorator(dividerHeight));

        ApiFactory.ApiByPage api = (ApiFactory.ApiByPage) getArguments().getSerializable(SERVICE_API_TAG);
        String title = getArguments().getString(TITLE_TAG);
        if (api == null) {
            throw new IllegalStateException("No Api object provided");
        }

        getActivity().setTitle(title);
        mPostLoadManager = new PostLoadManager(api);
        mPostLoadManager.setListener(new PostsLoadListener());

        int end = getResources().getDimensionPixelSize(R.dimen.refresh_layout_progress_view_end);
        mSwipeLayout.setProgressViewEndTarget(false, end);
        mSwipeLayout.setOnRefreshListener(mPostLoadManager::loadNew);

        if (savedInstanceState == null) {
            mPostLoadManager.loadNew();
            mSwipeLayout.setRefreshing(true);
        } else {
            mPostsAdapter.onRestoreAdapterState(savedInstanceState);
            Log.d(TAG, "onCreateView(), count = " + mPostsAdapter.getRealItemsCount());
            mPostsAdapter.notifyDataSetChanged();
        }
    }

    private void requestLoadMoreItems() {
        mPostLoadManager.loadFrom(mPostsAdapter.getRealItemsCount());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPostsAdapter.onSaveAdapterState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPostLoadManager.unSubscribeAll();
    }

    protected class PostsLoadListener implements PostLoadManager.LoadListener {
        @Override
        public void onStartLoading() {
            Log.d(TAG, "onStartLoading()");
            if (mPostsAdapter.getRealItemsCount() > 0) {
                mPostsAdapter.showFooter(PostListAdapter.FooterType.LOADING);
            } else {
                mPostsAdapter.hideFooter();
            }
            mPostsAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNewItemsLoaded(List<Post> posts) {
            Log.d(TAG, "onNewItemsLoaded()");
            mPostsAdapter.replaceItems(posts);

            if (posts.size() >= 1) {
                mPostsAdapter.showFooter(PostListAdapter.FooterType.FAIL);
            }
            mPostsAdapter.notifyDataSetChanged();

            onLoadingStopped();
        }

        @Override
        public void onAdditionalItemsLoaded(List<Post> posts) {
            Log.d(TAG, "onAdditionalItemsLoaded()");
            mPostsAdapter.addItems(posts);

            if (posts.size() >= 1) {
                mPostsAdapter.hideFooter();
            }
            mPostsAdapter.notifyDataSetChanged();
            onLoadingStopped();
        }

        @Override
        public void onAllItemsLoaded() {
            Log.d(TAG, "onAllItemsLoaded()");
            mPostsAdapter.hideFooter();
            mPostsAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoadingFailed(Throwable e) {
            Log.d(TAG, "onLoadingFailed()");
            onLoadingStopped();

            if (mPostsAdapter.getRealItemsCount() > 0) {
                mPostsAdapter.showFooter(PostListAdapter.FooterType.FAIL);
            } else {
                mPostsAdapter.hideFooter();
            }
            mPostsAdapter.notifyDataSetChanged();
        }

        void onLoadingStopped() {
            if (mSwipeLayout.isRefreshing()) {
                mSwipeLayout.setRefreshing(false);
            }
        }
    }

    private class AutoLoadScrollListener extends RecyclerView.OnScrollListener {
        private int visibleThreshold = 5;
        int firstVisibleItem, visibleItemCount, totalItemCount;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = recyclerView.getLayoutManager().getItemCount();

            firstVisibleItem = getFirstVisiblePosition(recyclerView);

            if (!mPostLoadManager.isLoading() && mPostLoadManager.hasMoreItemsToLoad() && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached
                Log.i(TAG, "fetch new items");
                requestLoadMoreItems();
            }
        }

        private int getFirstVisiblePosition(RecyclerView recyclerView) {
            int pos = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));
            return pos != RecyclerView.NO_POSITION ? pos : 0;
        }
    }
}
