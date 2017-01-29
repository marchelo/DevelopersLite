package com.marchelo.developerslite.model

import android.annotation.SuppressLint
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Gif example: {
 *  "id":14170,
 *  "description":"Новый сеньор обрушился с неконструктивной критикой на любимый командой проект...",
 *  "votes":17,
 *  "author":"100500",
 *  "date":"Jan 13, 2016 4:03:08 PM",
 *  "gifURL":"http://s1.developerslife.ru/public/images/gifs/f5a5dbef-d229-4b64-b26a-c29383a04aae.gif",
 *  "previewURL":"http://s1.developerslife.ru/public/images/previews/f5a5dbef-d229-4b64-b26a-c29383a04aae.jpg",
 *  "type":"gif"
 * }
 *
 * Coub example: {
 *  "id":14166,
 *  "description":"Проверка PixelPerfect верстки",
 *  "votes":-10,
 *  "author":"EaGames",
 *  "date":"Jan 12, 2016 8:19:29 AM",
 *  "previewURL":"http://storage.akamai.coub.com/get/b161/p/coub/simple/cw_image/ae645bbc5ad/a0bc684fae884bc0b5581/big_1452526251_00032.jpg",
 *  "embedId":"a9x4l",
 *  "type":"coub"
 * }
 *
 * @author Oleg Green
 * @since 23.08.15
 */
//TODO make it Parcelable
@DatabaseTable(tableName = "posts")
class Post2 : Serializable {
    @SuppressLint("SimpleDateFormat")
    val POST_DATE_FORMAT: DateFormat = SimpleDateFormat("MMM DD, yyyy hh:mm:ss a")

    class Column {
        val ID: String = "id"
        val POST_ID: String = "postId"
        val DESC: String = "description"
        val VOTES: String = "votes"
        val AUTHOR: String = "author"
        val DATE: String = "date"
        val PREVIEW_URL: String = "previewURL"
        val GIF_URL: String = "gifURL"
        val COUB_EMBED_ID: String = "embedId"
        val TYPE: String = "type"
    }

}