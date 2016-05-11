package com.marchelo.developerslite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Oleg Green
 * @since 26.01.16
 */

@DatabaseTable(tableName = "favorites")
public class Favorite {

    public static final class Column {
        public static final String ID = "id";
        public static final String POST_ID = "post_id";
        public static final String PREVIEW_URL = "preview_url";
        public static final String GIF_URL = "gif_url";
    }

    @SuppressWarnings("unused")
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, index = true, columnName = Column.ID)
    private long id;

    @SuppressWarnings("unused")
    @DatabaseField(index = true, unique = true, columnName = Column.POST_ID)
    private Long postId;

    @DatabaseField(columnName = Column.GIF_URL)
    private String gifURL;

    @DatabaseField(columnName = Column.PREVIEW_URL)
    private String previewURL;

    public Favorite() {
        //empty
    }

    public Favorite(long postId, String gifURL, String previewURL) {
        this.postId = postId;
        this.gifURL = gifURL;
        this.previewURL = previewURL;
    }

    public long getId() {
        return this.id;
    }

    public String getGifURL() {
        return gifURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }
}
