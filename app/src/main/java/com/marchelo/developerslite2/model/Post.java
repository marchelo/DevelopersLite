package com.marchelo.developerslite2.model;

import android.annotation.SuppressLint;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
@SuppressWarnings("unused")
@DatabaseTable(tableName = "posts")
public class Post implements Serializable {
    @SuppressLint("SimpleDateFormat")
    public static final DateFormat POST_DATE_FORMAT = new SimpleDateFormat("MMM DD, yyyy hh:mm:ss a");

    public static final class Column {
        public static final String ID = "id";
        public static final String POST_ID = "postId";
        public static final String DESC = "description";
        public static final String VOTES = "votes";
        public static final String AUTHOR = "author";
        public static final String DATE = "date";
        public static final String PREVIEW_URL = "previewURL";
        public static final String GIF_URL = "gifURL";
        public static final String COUB_EMBED_ID = "embedId";
        public static final String TYPE = "type";
    }

    @DatabaseField(generatedId = true, index = true, columnName = Column.ID)
    private long id;

    @Expose
    @SerializedName("id")
    @DatabaseField(index = true, unique = true, columnName = Column.POST_ID)
    private Long postId;

    @Expose
    @DatabaseField(columnName = Column.DESC)
    private String description;

    @Expose
    @DatabaseField(index = true, columnName = Column.VOTES)
    private Integer votes;

    @Expose
    @DatabaseField(columnName = Column.AUTHOR)
    private String author;

    @Expose
    @DatabaseField(columnName = Column.DATE, dataType = DataType.DATE_LONG)
    private Date date;

    @Expose
    @DatabaseField(columnName = Column.GIF_URL)
    private String gifURL;

    @Expose
    @DatabaseField(columnName = Column.PREVIEW_URL)
    private String previewURL;

    @Expose
    @DatabaseField(columnName = Column.COUB_EMBED_ID)
    private String coubEmbedId;

    @Expose
    @DatabaseField(columnName = Column.TYPE)
    private Type type;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

//    public String getDateString() {
//        return date;
//    }
//
//    public Date getDate() {
//        try {
//            return POST_DATE_FORMAT.parse(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return new Date();
//        }
//    }
//
//    public void setDate(Date date) {
//        this.date = POST_DATE_FORMAT.format(date);
//    }
//
//    public void setDateString(String date) {
//        this.date = date;
//    }

    public String getGifURL() {
        return gifURL;
    }

    public void setGifURL(String gifURL) {
        this.gifURL = gifURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isGif() {
        return this.type == Type.GIF;
    }

    public boolean isCoub() {
        return this.type == Type.COUB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (!postId.equals(post.postId)) return false;
        if (description != null ? !description.equals(post.description) : post.description != null)
            return false;
        if (votes != null ? !votes.equals(post.votes) : post.votes != null) return false;
        if (author != null ? !author.equals(post.author) : post.author != null) return false;
        if (date != null ? !date.equals(post.date) : post.date != null) return false;
        if (gifURL != null ? !gifURL.equals(post.gifURL) : post.gifURL != null) return false;

        return !(previewURL != null ? !previewURL.equals(post.previewURL) : post.previewURL != null)
                && !(type != null ? !type.equals(post.type) : post.type != null);

    }

    @Override
    public int hashCode() {
        int result = postId.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (votes != null ? votes.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (gifURL != null ? gifURL.hashCode() : 0);
        result = 31 * result + (previewURL != null ? previewURL.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId=" + postId +
                ", description='" + description + '\'' +
                ", votes=" + votes +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", gifURL='" + gifURL + '\'' +
                ", previewURL='" + previewURL + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public enum Type {
        @SerializedName("gif")
        GIF,
        @SerializedName("coub")
        COUB
    }
}
