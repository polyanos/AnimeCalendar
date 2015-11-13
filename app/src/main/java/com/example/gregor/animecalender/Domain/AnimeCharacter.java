package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 2-11-2015.
 */
public class AnimeCharacter {
    String name, url;
    int id;

    public AnimeCharacter(String name, int id){
        this.name = name;
        this.id = id;
    }
    public String getName(){return name;}

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
