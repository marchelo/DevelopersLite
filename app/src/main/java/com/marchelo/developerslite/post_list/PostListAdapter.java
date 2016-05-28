package com.marchelo.developerslite.post_list;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Post;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

/**
 * @author Oleg Green
 * @since 25.08.15
 */
public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Handler mUiHandler;
    private DbHelper mDbHelper;
    private OnTryAgainListener mTryLoadAgainListener;
    private FooterType mFooterType = FooterType.HIDDEN;

    @State
    ArrayList<Post> mPostList = new ArrayList<>(0);

    public PostListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mUiHandler = new Handler();
        mDbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
    }

    public void addItems(List<Post> newPosts) {
        mPostList.addAll(newPosts);
    }

    public void replaceItems(List<Post> newPosts) {
        mPostList.clear();
        mPostList.addAll(newPosts);
    }

    public void setTryAgainListener(OnTryAgainListener listener) {
        mTryLoadAgainListener = listener;
    }

    public void onRestoreAdapterState(Bundle savedState) {
        Icepick.restoreInstanceState(this, savedState);
    }

    public void onSaveAdapterState(Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new PostViewHolder(mInflater.inflate(R.layout.item_view_post, parent, false), mUiHandler, mDbHelper);
        } else if (viewType == 1) {
            return new FooterViewHolder(mInflater.inflate(R.layout.item_view_footer, parent, false), mTryLoadAgainListener);
        }
        throw new IllegalStateException("Cannot show item with type = " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            ((PostViewHolder) holder).bindData(mPostList.get(position));
        } else if (holder instanceof FooterViewHolder) {
            ((FooterViewHolder) holder).bindData(mFooterType);
        }
    }

    /**
     * Use {@link #getRealItemsCount()}
     */
    @Override
    @Deprecated
    public int getItemCount() {
        return mPostList.size() + (isFooterVisible() ? 1 : 0);
    }

    public int getRealItemsCount() {
        return mPostList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //noinspection deprecation
        if (isFooterVisible() && position == (getItemCount() - 1)) {
            return 1;
        }
        return 0;
    }

    public void showFooter(FooterType footerType) {
        mFooterType = footerType;
    }

    public void hideFooter() {
        mFooterType = FooterType.HIDDEN;
    }

    public boolean isFooterVisible() {
        return mFooterType != FooterType.HIDDEN;
    }

    public interface OnTryAgainListener {
        void onTryAgain();
    }

    public enum FooterType {
        FAIL, HIDDEN, LOADING
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.progress)
        ProgressBar progressBar;
        @BindView(R.id.tv_message)
        TextView messageView;
        @BindView(R.id.btn_try_again)
        View tryAgainView;

        private final OnTryAgainListener mListener;

        public FooterViewHolder(View itemView, OnTryAgainListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mListener = listener;
        }

        public void bindData(FooterType footerType) {
            switch (footerType) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    tryAgainView.setVisibility(View.GONE);
                    messageView.setText(R.string.post_list_loading_footer_loading);
                    break;
                case FAIL:
                    progressBar.setVisibility(View.GONE);
                    tryAgainView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.post_list_loading_footer_failed);
                    break;
                default:
                case HIDDEN:
                    throw new IllegalStateException("Cannot show footer with state = " + footerType.toString());
            }
        }

        @OnClick(R.id.btn_try_again)
        protected void tryLoadDataAgainRequested() {
            if (mListener != null) {
                mListener.onTryAgain();
            }
        }
    }
}



