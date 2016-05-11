package com.marchelo.developerslite.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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

/**
 * @author Oleg Green
 * @since 17.01.16
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {

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

    //TODO make it private
    public Dao<Post, Long> getPostDao() throws SQLException {
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

    public List<Favorite> getAllFavorites() throws SQLException {
        return getFavoriteDao().queryBuilder().orderBy(Favorite.Column.ID, false).query();
    }

    public void deleteFavorite(Favorite favoriteToDelete) throws SQLException {
        getFavoriteDao().delete(favoriteToDelete);
    }

    public void deleteFavorites(List<Favorite> favoritesToDelete) throws SQLException {
        getFavoriteDao().delete(favoritesToDelete);
    }

    public void addFavorite(Favorite favoriteToAdd) throws SQLException {
        getFavoriteDao().createIfNotExists(favoriteToAdd);
    }

    public void addFavorites(List<Favorite> favoritesToAdd) throws SQLException {
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

    public Favorite getFavoriteByPostId(long postId) throws SQLException {
        return getFavoriteDao().queryBuilder()
                .where().eq(Favorite.Column.POST_ID, postId)
                .queryForFirst();
    }
}