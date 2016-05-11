package com.marchelo.developerslite.manager;

import android.util.Log;

import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.model.PostsListHolder;
import com.marchelo.developerslite.network.ApiFactory;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Oleg Green
 * @since 29.08.15
 */
public class PostLoadManager {
    private static final String TAG = PostLoadManager.class.getSimpleName();
    public static final int PAGE_SIZE = 5;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private ApiFactory.ApiByPage mApi;
    private LoadListener mListener;
    private boolean mIsLoading;
    private boolean mHasMoreItemsToLoad = true;

    public PostLoadManager(ApiFactory.ApiByPage api) {
        mApi = api;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean hasMoreItemsToLoad() {
        return mHasMoreItemsToLoad;
    }

    public void setListener(LoadListener listener) {
        mListener = listener;
    }

    public void loadNew() {
        mHasMoreItemsToLoad = true;
        loadFrom(0);
    }

    public void loadFrom(final int position) {
        if (!mHasMoreItemsToLoad) {
            mListener.onAllItemsLoaded();
            return;
        }

        mIsLoading = true;
        mListener.onStartLoading();
        Observable<PostsListHolder> postsObservable = mApi.getByPage(position/PAGE_SIZE, PAGE_SIZE);
        Subscription sub = postsObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PostsListHolder>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");

                        mIsLoading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError()", e);

                        mIsLoading = false;
                        mListener.onLoadingFailed(e);
                    }

                    @Override
                    public void onNext(PostsListHolder postsListHolder) {
                        Log.d(TAG, "onNext()");

                        if (position == 0) {
                            mListener.onNewItemsLoaded(postsListHolder.getList());
                        } else {
                            mListener.onAdditionalItemsLoaded(postsListHolder.getList());
                        }

                        if (postsListHolder.getList().size() + position >= postsListHolder.getTotalCount()) {
                            mListener.onAllItemsLoaded();
                            mHasMoreItemsToLoad = false;
                        }
                    }
                });
        mCompositeSubscription.add(sub);
    }

    public void unSubscribeAll() {
        mIsLoading = false;
        mCompositeSubscription.unsubscribe();
    }

    public interface LoadListener {
        void onStartLoading();
        void onNewItemsLoaded(List<Post> posts);
        void onAdditionalItemsLoaded(List<Post> posts);
        void onAllItemsLoaded();
        void onLoadingFailed(Throwable e);
    }
}
