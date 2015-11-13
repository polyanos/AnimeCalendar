package com.example.gregor.animecalender.Utility.Interface;

import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Exceptions.AuthorizeException;

/**
 * Created by Gregor on 9-11-2015.
 */
public interface Api {
    String getApiDirectory();

    Anime getFullAnimeData(String ID);

    /**
     * Will authorize the app for usage of the api, if authorization isn't needed then this method should do nothing.
     * @throws AuthorizeException
     */
    void authorizeApi() throws AuthorizeException;
}
