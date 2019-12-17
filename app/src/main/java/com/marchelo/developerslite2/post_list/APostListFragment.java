package com.marchelo.developerslite2.post_list;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marchelo.developerslite2.R;
import com.marchelo.developerslite2.R2;
import com.marchelo.developerslite2.view.SmartRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public abstract class APostListFragment extends Fragment {
    protected static final String TITLE_TAG = "TITLE";

    protected QuickReturnScrollListener.ToolbarListener mListener;

    @BindView(R2.id.posts_list)
    protected SmartRecyclerView mPostListView;
    @BindView(R2.id.swipe_layout)
    protected SwipeRefreshLayout mSwipeLayout;
    @BindView(R2.id.empty_list_view)
    protected View mEmptyView;
    private Unbinder mUnBinder;

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
        mUnBinder = ButterKnife.bind(this, view);

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
        mUnBinder.unbind();
    }
}
