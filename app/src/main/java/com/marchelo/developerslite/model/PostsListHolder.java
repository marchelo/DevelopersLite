package com.marchelo.developerslite.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * @author Oleg Green
 * @since 23.08.15
 */
public class PostsListHolder {
    @Expose private List<Post> result = new ArrayList<>();
    @Expose private Integer totalCount;

    public PostsListHolder(List<Post> posts) {
        result = posts;
    }
    /**
     * @return The result
     */
    public List<Post> getList() {
        return result;
    }

    /**
     * @return The totalCount
     */
    public Integer getTotalCount() {
        return totalCount;
    }
}