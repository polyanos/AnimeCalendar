package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 27-10-2015.
 */
public class ImageToLoad {
    String imageName;
    String imageUrl;
    boolean saveInCache;

    public ImageToLoad(String imageName, String imageUrl, boolean saveInCache) {
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.saveInCache = saveInCache;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean saveInCache() {
        return saveInCache;
    }
}
