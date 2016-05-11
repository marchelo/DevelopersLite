package com.marchelo.developerslite.post_list;

import android.os.Bundle;
import android.text.TextUtils;

import com.marchelo.developerslite.manager.PostLoadManager;
import com.marchelo.developerslite.network.ApiFactory;

import java.util.ArrayList;

/**
 * @author Oleg Green
 * @since 16.01.16
 */
public class SearchPostsFragment extends PostListFragment {

    public static SearchPostsFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putSerializable(SERVICE_API_TAG, ApiFactory.searchPostsApi(query));
        args.putString(TITLE_TAG, null);
        SearchPostsFragment postListFragment = new SearchPostsFragment();
        postListFragment.setArguments(args);

        return postListFragment;
    }

//    public void doSearch(String query) {
//        mPostLoadManager = new PostLoadManager(ApiFactory.searchPostsApi(query));
//        mPostLoadManager.setListener(new PostsLoadListener());
//
//        if (TextUtils.isEmpty(query)) {
//            mPostsAdapter.replaceItems(new ArrayList<>(0));
//            mPostsAdapter.hideFooter();
//            mPostsAdapter.notifyDataSetChanged();
//            mSwipeLayout.setRefreshing(false);
//
//        } else {
//            mPostLoadManager.loadNew();
//            mSwipeLayout.setRefreshing(true);
//        }
//    }
}
