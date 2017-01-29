package com.marchelo.developerslite.model

import java.util.ArrayList

import com.google.gson.annotations.Expose

/**
 * @author Oleg Green
 * *
 * @since 23.08.15
 */
class PostsListHolder(posts: List<Post>) {
    /**
     * @return The result
     */
    @Expose
    val list: List<Post> = posts
    /**
     * @return The totalCount
     */
    @Expose
    val totalCount: Int? = posts.count();
}