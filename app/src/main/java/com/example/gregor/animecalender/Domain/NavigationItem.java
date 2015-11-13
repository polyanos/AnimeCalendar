package com.example.gregor.animecalender.Domain;

import android.graphics.drawable.Drawable;

/**
 * Created by Gregor on 13-11-2015.
 */
public class NavigationItem {
    String title;
    Drawable icon;
    Class activity;

    public NavigationItem(String title, Drawable icon, Class activity) {
        this.title = title;
        this.icon = icon;
        this.activity = activity;
    }

    public String getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Class getActivity() {
        return activity;
    }
}
