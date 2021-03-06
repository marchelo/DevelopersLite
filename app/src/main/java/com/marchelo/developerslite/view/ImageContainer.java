package com.marchelo.developerslite.view;

/**
 * @author Oleg Green
 * @since 12.09.15
 */
public interface ImageContainer {
    void setAspectRatio(float aspectRatio);
    float getAspectRatio();
    void setMaxHeight(int maxHeight);
    void invalidate();
}
