package com.marchelo.developerslite.model

import com.google.gson.annotations.Expose

import java.util.ArrayList

/**
 * @author Oleg Green
 * *
 * @since 16.05.16
 */
class CommentsListHolder {
    @Expose
    val comments: List<Comment> = ArrayList()

    override fun toString(): String {
        return "CommentsListHolder{" +
                "comments=" + comments +
                '}'
    }
}
