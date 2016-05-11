package com.marchelo.developerslite.post_list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.view.SmartRecyclerView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public abstract class APostListFragment extends Fragment {
    protected static final String TITLE_TAG = "TITLE";

    protected QuickReturnScrollListener.ToolbarListener mListener;

    @Bind(R.id.posts_list)
    protected SmartRecyclerView mPostListView;
    @Bind(R.id.swipe_layout)
    protected SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.empty_list_view)
    protected View mEmptyView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (QuickReturnScrollListener.ToolbarListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("activity '" + context.getClass().getSimpleName()
                    + "' should implement '" + QuickReturnScrollListener.ToolbarListener.class.getSimpleName() + "'");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            getActivity().setTitle(getArguments().getString(TITLE_TAG));
        }

        onCreateView(savedInstanceState);

        return view;
    }

    protected abstract void onCreateView(Bundle savedInstanceState);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
