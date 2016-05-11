package com.marchelo.developerslite.network;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.model.PostsListHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

/**
 * @author Oleg Green
 * @since 24.08.15
 */
public class ApiFactory {
    private static DevLifeService WEB_SERVICE;

    static {
        Gson gson = new GsonBuilder()
                .setDateFormat("MMM DD, yyyy hh:mm:ss a")
                .excludeFieldsWithoutExposeAnnotation()
//                .registerTypeAdapter(Date.class, new DateDeserializer())
                .create();
//
//        String date = "Aug 16, 2013 12:33:00 PM";
//        Log.d("test2", " result date = " + POST_DATE_FORMAT.format(new Date()));

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.WEB_SERVICE_BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .build();
        WEB_SERVICE = restAdapter.create(DevLifeService.class);
    }

    public static ApiByPage lastPostsApi() {
        return (pageNumber, pageSize) -> WEB_SERVICE.getPosts(
                DevLifeService.LATEST_CATEGORY,
                pageNumber,
                pageSize,
                DevLifeService.GIF_TYPE);
    }

    public static ApiByPage hotPostsApi() {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getPosts(
                        DevLifeService.HOT_CATEGORY,
                        pageNumber,
                        pageSize,
                        DevLifeService.GIF_TYPE);
    }

    public static ApiByPage bestOfAllPostsApi() {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getPosts(
                        DevLifeService.BEST_OF_ALL_CATEGORY,
                        pageNumber,
                        pageSize,
                        DevLifeService.GIF_TYPE);
    }

    public static ApiByPage bestOfMonthPostsApi() {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getPosts(
                        DevLifeService.BEST_OF_MONTH_CATEGORY,
                        pageNumber,
                        pageSize,
                        DevLifeService.GIF_TYPE);
    }

    public static ApiByPage bestOfWeekPostsApi() {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getPosts(
                        DevLifeService.BEST_OF_WEEK_CATEGORY,
                        pageNumber,
                        pageSize,
                        DevLifeService.GIF_TYPE);
    }

    public static ApiByPage bestOfDayPostsApi() {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getPosts(
                        DevLifeService.BEST_OF_DAY_CATEGORY,
                        pageNumber,
                        pageSize,
                        DevLifeService.GIF_TYPE);
    }

    //TODO refactor
    public static ApiByPage randomApi() {
        return new ApiByPage() {
            @Override
            public Observable<PostsListHolder> getByPage(int pageNumber, int pageSize) {
                return getRandomPosts();
            }

            @NonNull
            private Observable<PostsListHolder> getRandomPosts() {
                return Observable.zip(
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        WEB_SERVICE.getRandomPost(),
                        (p1, p2, p3, p4, p5, p6, p7, p8, p9) -> verify(p1, p2, p3, p4, p5, p6, p7, p8, p9));
            }

            private PostsListHolder verify(Post... posts) {
                List<Post> list = new ArrayList<>();
                for (Post post: posts) {
                    if (post.isGif()) {
                        list.add(post);
                    }
                }
                return new PostsListHolder(list);
            }
        };
    }

    public static ApiByPage searchPostsApi(String searchQuery) {
        return (pageNumber, pageSize) ->
                WEB_SERVICE.getSearchedPosts(
                        pageNumber,
                        searchQuery);
    }

    public static ApiPostById postByIdApi() {
        return WEB_SERVICE::getPostById;
    }

    //TODO use other way instead of Serializable
    public interface ApiByPage extends Serializable {
        Observable<PostsListHolder> getByPage(int pageNumber, int pageSize);
    }

    public interface ApiPostById {
        Observable<Post> getPostById(int id);
    }
}
