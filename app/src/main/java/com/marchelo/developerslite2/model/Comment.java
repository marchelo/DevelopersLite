package com.marchelo.developerslite2.model;

import com.google.gson.annotations.Expose;

import java.util.Date;

/**
 * Comment example: {
 *  "id":175,
 *  "text":"напишите название фильма :)",
 *  "date":"15.05.2013 12:02",
 *  "voteCount":3,
 *  "authorId":814,
 *  "authorName":"misha",
 *  "parentId":39677,
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
    private int id;

    @Expose
    private String text;

    @Expose
    private Date date;

    @Expose
    private int voteCount;

    @Expose
    private int authorId;

    @Expose
    private String authorName;

    @Expose
    private int parentId;

    @Expose
    private int entryId;

    @Expose
    private boolean deleted;

    @Expose
    private boolean voted;

    @Expose
    private boolean editable;

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public int getEntryId() {
        return entryId;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        if (id != comment.id) return false;
        if (voteCount != comment.voteCount) return false;
        if (authorId != comment.authorId) return false;
        if (entryId != comment.entryId) return false;
        if (deleted != comment.deleted) return false;
        if (voted != comment.voted) return false;
        if (editable != comment.editable) return false;
        if (text != null ? !text.equals(comment.text) : comment.text != null) return false;
        if (date != null ? !date.equals(comment.date) : comment.date != null) return false;
        return authorName != null ? authorName.equals(comment.authorName) : comment.authorName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + voteCount;
        result = 31 * result + authorId;
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 31 * result + entryId;
        result = 31 * result + (deleted ? 1 : 0);
        result = 31 * result + (voted ? 1 : 0);
        result = 31 * result + (editable ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", voteCount=" + voteCount +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", entryId=" + entryId +
                ", deleted=" + deleted +
                ", voted=" + voted +
                ", editable=" + editable +
                '}';

    }
}
