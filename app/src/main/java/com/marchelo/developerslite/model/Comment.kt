package com.marchelo.developerslite.model

import com.google.gson.annotations.Expose

import java.util.Date

/**
 * Comment example: {
 * "id":175,
 * "text":"напишите название фильма :)",
 * "date":"15.05.2013 12:02",
 * "voteCount":3,
 * "authorId":814,
 * "authorName":"misha",
 * "parentId":39677,
 * "entryId":100,
 * "deleted":false,
 * "voted":false,
 * "editable":false
 * }

 * @author Oleg Green
 *
 * @since 16.05.16
 */
class Comment {

    @Expose
    val id: Int = 0

    @Expose
    val text: String? = null

    @Expose
    val date: Date? = null

    @Expose
    val voteCount: Int = 0

    @Expose
    private val authorId: Int = 0

    @Expose
    val authorName: String? = null

    @Expose
    val parentId: Int = 0

    @Expose
    val entryId: Int = 0

    @Expose
    private val deleted: Boolean = false

    @Expose
    private val voted: Boolean = false

    @Expose
    private val editable: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val comment = other as Comment?

        if (id != comment!!.id) return false
        if (voteCount != comment!!.voteCount) return false
        if (authorId != comment!!.authorId) return false
        if (entryId != comment!!.entryId) return false
        if (deleted != comment!!.deleted) return false
        if (voted != comment!!.voted) return false
        if (editable != comment!!.editable) return false
        if (if (text != null) text != comment!!.text else comment!!.text != null) return false
        if (if (date != null) date != comment.date else comment.date != null) return false
        return if (authorName != null) authorName == comment.authorName else comment.authorName == null
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (date?.hashCode() ?: 0)
        result = 31 * result + voteCount
        result = 31 * result + authorId
        result = 31 * result + (authorName?.hashCode() ?: 0)
        result = 31 * result + entryId
        result = 31 * result + if (deleted) 1 else 0
        result = 31 * result + if (voted) 1 else 0
        result = 31 * result + if (editable) 1 else 0
        return result
    }

    override fun toString(): String {
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
                '}'
    }
}