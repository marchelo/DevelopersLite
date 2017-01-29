package com.marchelo.developerslite.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * @author Oleg Green
 * *
 * @since 26.01.16
 */

@DatabaseTable(tableName = "favorites")
class Favorite {

    object Column {
        val ID = "id"
        val POST_ID = "post_id"
        val PREVIEW_URL = "preview_url"
        val GIF_URL = "gif_url"
    }

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, index = true, columnName = Column.ID)
    val id: Long = 0

    @DatabaseField(index = true, unique = true, columnName = Column.POST_ID)
    private val postId: Long?

    @DatabaseField(columnName = Column.GIF_URL)
    val gifURL: String

    @DatabaseField(columnName = Column.PREVIEW_URL)
    val previewURL: String

    constructor() {
        //empty
    }

    constructor(postId: Long, gifURL: String, previewURL: String) {
        this.postId = postId
        this.gifURL = gifURL
        this.previewURL = previewURL
    }

    fun getPostId(): Long {
        return postId!!
    }

    companion object {

        fun createFrom(post: Post): Favorite {
            return Favorite(post.postId!!, post.gifURL, post.previewURL)
        }
    }
}
