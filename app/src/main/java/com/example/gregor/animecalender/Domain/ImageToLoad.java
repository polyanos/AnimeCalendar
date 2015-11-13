package com.example.gregor.animecalender.Domain;

import android.support.annotation.NonNull;

/**
 * Created by Gregor on 27-10-2015.
 */
public class ImageToLoad extends FileToLoad {
    String imageUrl;
    boolean saveInCache, cropImage;
    Dimension newDimensions;

    /**
     *
     * @param fileName
     * @param fileDirectory
     * @param imageUrl
     * @param saveInCache
     */
    public ImageToLoad(@NonNull String fileName, @NonNull String fileDirectory, @NonNull String imageUrl, boolean saveInCache) {
        this(fileName, fileDirectory, imageUrl, saveInCache, false, null);
    }

    /**
     *
     * @param fileName
     * @param fileDirectory
     * @param imageUrl
     * @param saveInCache
     * @param cropImage
     * @param newDimensions
     */
    public ImageToLoad(@NonNull String fileName, @NonNull String fileDirectory, @NonNull String imageUrl, boolean saveInCache, boolean cropImage, Dimension newDimensions) {
        super(fileName, fileDirectory);
        this.imageUrl = imageUrl;
        this.saveInCache = saveInCache;
        this.cropImage = cropImage;
        this.newDimensions = newDimensions;
    }

    public boolean cropImage() {
        return cropImage;
    }

    public Dimension getNewDimensions() {
        return newDimensions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isSaveInCache() {
        return saveInCache;
    }
}
