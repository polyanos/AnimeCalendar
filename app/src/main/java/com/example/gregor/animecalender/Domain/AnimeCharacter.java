package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 2-11-2015.
 */
public class AnimeCharacter {
    String firstName, lastName, imageName;
    int id;

    public AnimeCharacter(String firstName, String lastName, int id){
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.imageName = "c" + id + ".jpg";
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getImageName() {
        return imageName;
    }

    public int getId() {
        return id;
    }
}
