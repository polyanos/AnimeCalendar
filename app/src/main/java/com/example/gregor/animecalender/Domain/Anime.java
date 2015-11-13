package com.example.gregor.animecalender.Domain;

import java.util.List;

/**
 * Created by Gregor on 26-10-2015.
 */
public class Anime {
    private String japaneseTitle, romanjiTitle, description, imageUrl;
    private List<String> gerneArray;
    private List<AnimeCharacter> animeCharacters;
    private int id, episodeTotal;
    private long startDate, endDate;

    public Anime(int id){
        this.japaneseTitle = "";
        this.romanjiTitle = "";
        this.id = id;
    }

    public Anime(String japaneseTitle, String romanjiTitle) {
        this.japaneseTitle = japaneseTitle;
        this.romanjiTitle = romanjiTitle;
        this.id = 0;
    }

    public Anime(String japaneseTitle, String romanjiTitle, int id) {
        this.japaneseTitle = japaneseTitle;
        this.romanjiTitle = romanjiTitle;
        this.id = id;
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

    public List<String> getGerneArray() {
        return gerneArray;
    }

    public void setGerneArray(List<String> gerneArray) {
        this.gerneArray = gerneArray;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<AnimeCharacter> getAnimeCharacters() {
        return animeCharacters;
    }

    public void setJapaneseTitle(String japaneseTitle) {
        this.japaneseTitle = japaneseTitle;
    }

    public void setRomanjiTitle(String romanjiTitle) {
        this.romanjiTitle = romanjiTitle;
    }

    public void setAnimeCharacters(List<AnimeCharacter> animeCharacters) {
        this.animeCharacters = animeCharacters;
    }
}
