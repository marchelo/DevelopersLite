package com.marchelo.developerslite.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.marchelo.developerslite.model.CommentsListHolder;
import com.marchelo.developerslite.model.Post;
import com.marchelo.developerslite.model.PostsListHolder;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * @author Oleg Green
 * @since 24.08.15
 */
public class ApiFactory {
    private static DevLifeService WEB_SERVICE;

    static {
        Gson gson = new GsonBuilder()
//                .setDateFormat("MMM DD, yyyy hh:mm:ss a")
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();
//
//        String date = "Aug 16, 2013 12:33:00 PM";
//        Log.d("test2", " result date = " + POST_DATE_FORMAT.format(new Date()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEB_SERVICE_BASE_URL)
                //TODO
//                .setLogLevel(RestAdapter.LogLevel.FULL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        WEB_SERVICE = retrofit.create(DevLifeService.class);
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
        return new ApiPostById() {
            @Override
            public Observable<Post> getPostById(long id) {
                return WEB_SERVICE.getPostById(id);
            }

            @Override
            public Observable<CommentsListHolder> getCommentsByPostId(long postId) {
                return WEB_SERVICE.getComments(postId);
            }
        };
    }

    //TODO use other way instead of Serializable
    public interface ApiByPage extends Serializable {
        Observable<PostsListHolder> getByPage(int pageNumber, int pageSize);
    }

    public interface ApiPostById {
        Observable<Post> getPostById(long id);
        Observable<CommentsListHolder> getCommentsByPostId(long postId);
    }


    private static class DateDeserializer implements JsonDeserializer<Date> {
        private final String[] DATE_FORMATS = new String[] {
                "MMM DD, yyyy hh:mm:ss a",      // "Aug 16, 2013 12:33:00 PM";
                "dd.MM.yyyy HH:mm"              // "15.05.2013 12:02"
        };

        @Override
        public Date deserialize(JsonElement jsonElement, Type typeOF,
                                JsonDeserializationContext context) throws JsonParseException {
            for (String format : DATE_FORMATS) {
                try {
                    return new SimpleDateFormat(format, Locale.US).parse(jsonElement.getAsString());
                } catch (ParseException e) {
                    Log.d("deserialize", "deserialize: ", e);
                }
            }
            throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
                    + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
        }
    }
}
