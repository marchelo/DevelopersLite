package com.marchelo.developerslite.model;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Comment example: {
 *  "id":175,
 *  "text":"напишите название фильма :)",
 *  "date":"15.05.2013 12:02",
 *  "voteCount":3,
 *  "authorId":814,
 *  "authorName":
 *  "misha",
 *  "entryId":100,
 *  "deleted":false,
 *  "voted":false,
 *  "editable":false
 * }
 *
 * @author Oleg Green
 * @since 16.05.16
 */
public class Comment {
    @Expose
    private String text;

    @Expose
    private Date date;

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", date=" + date +
                '}';
    }
}
