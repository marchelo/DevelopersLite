package com.marchelo.developerslite.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.marchelo.developerslite.R;
import com.marchelo.developerslite.model.Favorite;
import com.marchelo.developerslite.model.Post;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;

    /**
     * The data access object used to interact with the Sqlite database to do C.R.U.D operations.
     */
    private Dao<Post, Long> postDao;
    private Dao<Favorite, Long> favoriteDao;

    public static DbHelper from(Context context) {
        return OpenHelperManager.getHelper(context, DbHelper.class);
    }

    public DbHelper(Context context) {
        /**
         * R.raw.ormlite_config is a reference to the ormlite_config.txt file in the
         * /res/raw/ directory of this project
         * */
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Post.class);
            TableUtils.createTable(connectionSource, Favorite.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            /**
             * Recreates the database when onUpgrade is called by the framework
             */
            TableUtils.dropTable(connectionSource, Post.class, false);
            TableUtils.dropTable(connectionSource, Favorite.class, false);
            onCreate(database, connectionSource);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //used in loader
    public List<Post> getAllPosts() throws SQLException {
        return getPostDao().queryBuilder().orderBy(Post.Column.ID, false).query();
    }

    public Observable<Post> getPostByPostIdAsync(long postId) {
        return makeSimpleDbObservable(() -> DbHelper.this.getPostByPostId(postId));
    }

    public Observable<Boolean> addPostIfAbsentAsync(Post post) {
        return makeSimpleDbObservable(() -> {
            if (getPostByPostId(post.getPostId()) != null) {
                return false;
            }
            addPost(post);
            return true;
        });
    }

    public Observable<Boolean> deletePostIfPresentAsync(Post postToDelete) {
        return makeSimpleDbObservable(() -> {
            Post post = getPostByPostId(postToDelete.getPostId());
            if (post == null) {
                return false;
            }
            deletePostById(post.getId());
            return true;
        });
    }

    //used in loader
    public List<Favorite> getAllFavorites() throws SQLException {
        return getFavoriteDao().queryBuilder().orderBy(Favorite.Column.ID, false).query();
    }

    public Observable<Favorite> getFavoriteByPostIdAsync(long postId) {
        return makeSimpleDbObservable(() -> DbHelper.this.getFavoriteByPostId(postId));
    }

    public Observable<Boolean> addFavoriteIfAbsentAsync(Favorite favoriteToAdd) {
        return makeSimpleDbObservable(() -> {
            if (getFavoriteByPostId(favoriteToAdd.getPostId()) != null) {
                return false;
            }
            addFavorite(favoriteToAdd);
            return true;
        });
    }

    public Observable<Boolean> addFavoritesAsync(List<Favorite> favoritesToAdd) {
        return makeSimpleDbObservable(() -> {
            addFavorites(favoritesToAdd);
            return true;
        });
    }

    public Observable<Boolean> deleteFavoriteIfPresentAsync(long postId) {
        return makeSimpleDbObservable(() -> {
            Favorite favorite = getFavoriteByPostId(postId);
            if (favorite == null) {
                return false;
            }
            deleteFavorite(favorite);
            return true;
        });
    }

    public Observable<Boolean> deleteFavoritesAsync(List<Favorite> favoritesToDelete) {
        return makeSimpleDbObservable(() -> {
            deleteFavorites(favoritesToDelete);
            return true;
        });
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////// private /////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    private Post getPostByPostId(long postId) throws SQLException {
        return getPostDao().queryBuilder()
                .where().eq(Post.Column.POST_ID, postId)
                .queryForFirst();
    }

    private int addPost(Post post) throws SQLException {
        return getPostDao().create(post);
    }

    private int deletePostById(long postId) throws SQLException {
        return getPostDao().deleteById(postId);
    }

    private Favorite getFavoriteByPostId(long postId) throws SQLException {
        return getFavoriteDao().queryBuilder()
                .where().eq(Favorite.Column.POST_ID, postId)
                .queryForFirst();
    }

    private void addFavorite(Favorite favoriteToAdd) throws SQLException {
        getFavoriteDao().createIfNotExists(favoriteToAdd);
    }

    private void addFavorites(List<Favorite> favoritesToAdd) throws SQLException {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            for (Favorite favoriteToAdd : favoritesToAdd) {
                addFavorite(favoriteToAdd);
            }
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    private void deleteFavorite(Favorite favoriteToDelete) throws SQLException {
        getFavoriteDao().delete(favoriteToDelete);
    }

    private void deleteFavorites(List<Favorite> favoritesToDelete) throws SQLException {
        getFavoriteDao().delete(favoritesToDelete);
    }

    private Dao<Post, Long> getPostDao() throws SQLException {
        if(postDao == null) {
            postDao = getDao(Post.class);
        }
        return postDao;
    }

    private Dao<Favorite, Long> getFavoriteDao() throws SQLException {
        if(favoriteDao == null) {
            favoriteDao = getDao(Favorite.class);
        }
        return favoriteDao;
    }

    private static <T> Observable<T> makeSimpleDbObservable(final Callable<T> func) {
        return Observable.create(
                new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        try {
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }

                            subscriber.onNext(func.call());
                            subscriber.onCompleted();

                        } catch (Exception e) {
                            Log.d(TAG, "call: ", e);
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }
}