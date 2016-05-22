package com.marchelo.developerslite.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Green
 * @since 16.05.16
 */
public class CommentsListHolder {
    @Expose
    private List<Comment> comments = new ArrayList<>();

    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "CommentsListHolder{" +
                "comments=" + comments +
                '}';
    }
}
