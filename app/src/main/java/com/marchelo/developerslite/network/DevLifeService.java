package com.marchelo.developerslite.network;

import androidx.annotation.IntRange;
import androidx.annotation.StringDef;

import com.marchelo.developerslite.model.CommentsListHolder;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.model.PostsListHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author Oleg Green
 * @since 23.08.15
 */
interface DevLifeService {

    String LATEST_CATEGORY = "latest";
    String HOT_CATEGORY = "hot";
    String BEST_OF_ALL_CATEGORY = "top";
    String BEST_OF_MONTH_CATEGORY = "monthly";
    String BEST_OF_WEEK_CATEGORY = "weekly";
    String BEST_OF_DAY_CATEGORY = "daily";

    @StringDef({LATEST_CATEGORY, HOT_CATEGORY, BEST_OF_ALL_CATEGORY,
            BEST_OF_MONTH_CATEGORY, BEST_OF_WEEK_CATEGORY, BEST_OF_DAY_CATEGORY})
    @Retention(RetentionPolicy.SOURCE)
    @interface CATEGORY {}

    String GIF_TYPE = "gif";
    String COUB_TYPE = "coub";
    String ALL_TYPE = "gif,coub";

    @StringDef({GIF_TYPE, COUB_TYPE, ALL_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    @interface MEDIA_TYPE {}

    @GET("/{category}/{page}?json=true")
    Observable<PostsListHolder> getPosts(
            @Path("category")   @CATEGORY                   String category,
            @Path("page")       @IntRange(from=0)           int page,
            @Query("pageSize")  @IntRange(from=5, to=50)    int pageSize,
            @Query("types")     @MEDIA_TYPE                  String imageType
    );

    @GET("/search?json=true")
    Observable<PostsListHolder> getSearchedPosts(
            @Query("page")      @IntRange(from=0)    int pageNumber,
            @Query("phrase")    String searchQuery
    );

    @GET("/{id}?json=true")
    Observable<Post> getPostById(@Path("id") @IntRange(from=0) long id);

    @GET("/random?json=true")
    Observable<Post> getRandomPost();

    @GET("/comments/entry/{id}")
    Observable<CommentsListHolder> getComments(@Path("id") @IntRange(from=0) long id);
}