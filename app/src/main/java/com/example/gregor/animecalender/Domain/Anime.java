package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 26-10-2015.
 */
public class Anime {
    private String japaneseTitle, romanjiTitle, description, imageUrl, imageFileName;
    private String[] gerneArray;
    private AnimeCharacter[] animeCharacters;
    private int id, episodeTotal;
    private long startDate, endDate;
    private boolean imageDownloaded;

    public Anime(String japaneseTitle, String romanjiTitle) {
        this.japaneseTitle = japaneseTitle;
        this.romanjiTitle = romanjiTitle;
        this.id = 0;
        setFileName();
    }

    public Anime(String japaneseTitle, String romanjiTitle, int id) {
        this.japaneseTitle = japaneseTitle;
        this.romanjiTitle = romanjiTitle;
        this.id = id;
        setFileName();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public int getEpisodeTotal() {
        return episodeTotal;
    }

    public void setEpisodeTotal(int episodeTotal) {
        this.episodeTotal = episodeTotal;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getJapaneseTitle() {
        return japaneseTitle;
    }

    public String getRomanjiTitle() {
        return romanjiTitle;
    }

    public int getId() {
        return id;
    }

    public String[] getGerneArray() {
        return gerneArray;
    }

    public void setGerneArray(String[] gerneArray) {
        this.gerneArray = gerneArray;
    }

    public void setId(int id) {
        this.id = id;
        setFileName();
    }

    public AnimeCharacter[] getAnimeCharacters() {
        return animeCharacters;
    }

    public void setAnimeCharacters(AnimeCharacter[] animeCharacters) {
        this.animeCharacters = animeCharacters;
    }

    private void setFileName() {
        imageFileName = String.valueOf(id) + ".jpg";
    }
}
