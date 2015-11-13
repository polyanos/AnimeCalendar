package com.example.gregor.animecalender.Utility;

/**
 * Created by Gregor on 27-10-2015.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.content.Context;
import android.text.Html;
import android.util.Log;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Domain.AnimeCharacter;
import com.example.gregor.animecalender.Domain.FileToLoad;
import com.example.gregor.animecalender.Domain.Parameter;
import com.example.gregor.animecalender.Exceptions.AuthorizeException;
import com.example.gregor.animecalender.Exceptions.HttpResponseException;
import com.example.gregor.animecalender.Utility.Interface.Api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Gregor
 */
public class AnilistApi implements Api {

    public static final String NAME = "AniList";
    private static final String FOLDER_PREFIX = "anilist";
    private static final String TAG = "AnilistApi";
    private static final String PREFIX = "https://anilist.co/api/";
    private static final String IMAGE_PREFIX = "http://anilist.co/img/dir/anime/reg/";
    private static final String CLIENT_ID = "polyanos-6bzkg";
    private static final String CLIENT_SECRET = "t7XXJN3OFZmDvR6m389zBPkwkW";

    private String accessToken;
    private Context applicationContext;

    public AnilistApi(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void authorizeApi() throws AuthorizeException {
        String url = "auth/access_token";
        JsonObject jsonResult;

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("grant_type", "client_credentials"));
        parameters.add(new Parameter("client_id", CLIENT_ID));
        parameters.add(new Parameter("client_secret", CLIENT_SECRET));

        try {
            HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(PREFIX + url, parameters), "POST");
            if (conn.getResponseCode() == 200) {
                jsonResult = (JsonObject) getJsonResponse(conn.getInputStream());
                accessToken = jsonResult.getString("access_token", "");
            } else {
                throw new HttpResponseException();
            }
        } catch (MalformedURLException ex) {
            Log.wtf(TAG, "Developer needs to check the generated url is incorrect. (" + ex.getMessage() + ")");
        } catch (IOException | HttpResponseException ex) {
            Log.e(TAG, "An error has occured while establishing the connection, the following error message was given: " + ex.getMessage());

            throw new AuthorizeException(ex);
        }
    }

    /**
     * Gets "small anime models" from the anilist api for each anime that adheres to the supplied parameters. This data won't be cached momentarily.
     * The retrieved data will be parsed and then stored in a Anime object.
     *
     * @param season The season it should look up. (Winter, Spring, Summer, Spring)
     * @param year The year it should look up. (xxxx)
     * @param type The type it should loom up. (Tv, Movie, Ova)
     * @return
     */
    public List<Anime> getSeasonAnime(String season, String year, String type) {
        String url = "browse/anime";

        JsonArray jsonResult = new JsonArray();
        List<Anime> animeList = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("access_token", accessToken));
        parameters.add(new Parameter("year", year));
        parameters.add(new Parameter("season", season));
        parameters.add(new Parameter("full_page", "true"));
        parameters.add(new Parameter("type", type));

        String japanese_title, romanji_title, image_url;
        int id;

        try {
            HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(PREFIX + url, parameters), "GET");
            Log.d(TAG, "Contacting the api with the following url: " + conn.getURL().toString());
            if (conn.getResponseCode() == 200) {
                jsonResult = (JsonArray) getJsonResponse(conn.getInputStream());
            } else {
                throw new HttpResponseException("The server response was: " + conn.getResponseCode());
            }

        } catch (MalformedURLException ex) {
            System.err.println("The api url for requesting anime season data is incorrect.");
        } catch (IOException | HttpResponseException ex) {
            System.err.println("An error has occured while establishing the connection, the following error message was given: ");
            System.err.println(ex.getMessage());
        }

        for (Iterator<JsonValue> it = jsonResult.iterator(); it.hasNext(); ) {
            JsonObject animeJsonObject = it.next().asObject();
            japanese_title = getStringValue(animeJsonObject, "title_japanese", "");
            romanji_title = getStringValue(animeJsonObject, "title_romaji", "");
            image_url = getStringValue(animeJsonObject, "image_url_lge", "");
            id = getIntValue(animeJsonObject, "id", 0);
            Anime animeObject = new Anime(japanese_title, romanji_title, id);
            animeObject.setImageUrl(image_url);
            animeList.add(animeObject);
        }
        return animeList;
    }

    /**
     * Return the directory which should be used for this api for caching purposes.
     * @return
     */
    @Override
    public String getApiDirectory() {
        return FOLDER_PREFIX;
    }

    /**
     * Retrieves anime data from the AniList api.
     * It will parse the data and return the parsed data in an Anime object.
     * If this is the first time this data has been retrieved (or if the cache has been cleared) this method will also write a copy of the retrieved data to the file system)
     * If the data already exists in the filesystem it will use that data and skip contacting the server.
     * @param animeId The id of the anime that will be retrieved.
     * @return An filled Anime object.
     */
    public Anime getFullAnimeData(String animeId) {
        String url = "anime/" + animeId + "/characters";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Anime anime = null;
        InputStream fileInput = null;
        InputStream webInput = null;

        try {
            FileCache fileCache = new FileCache(applicationContext);
            FileToLoad fileToLoad = new FileToLoad(animeId, fileCache.getStandardXmlDirectory());
            JsonObject jsonObject;
            if ((fileInput = fileCache.loadXmlFile(fileToLoad, this)) != null) {
                jsonObject = (JsonObject) getJsonResponse(fileInput);
            } else {
                List<Parameter> parameter = new ArrayList<>();
                parameter.add(new Parameter("access_token", accessToken));
                HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(PREFIX + url, parameter), "GET");
                if (conn.getResponseCode() == 200) {
                    webInput = conn.getInputStream();
                    fileCache.saveXmlFile(webInput, fileToLoad, this);
                    fileInput = fileCache.loadXmlFile(fileToLoad, this);
                    jsonObject = (JsonObject) getJsonResponse(fileInput);
                } else {
                    throw new HttpResponseException("The server response was: " + conn.getResponseCode());
                }
            }

            String japanese_title = getStringValue(jsonObject, "title_japanese", "");
            String romanji_title = getStringValue(jsonObject, "title_romaji", "");
            String image_url = getStringValue(jsonObject, "image_url_lge", "");
            String description = Html.fromHtml(getStringValue(jsonObject, "description", "")).toString();
            String startdateString = getStringValue(jsonObject, "start_date", "");
            String enddateString = getStringValue(jsonObject, "end_date", "");
            List<String> gerneList = getGerneList(jsonObject);
            List<AnimeCharacter> animeCharacters = getCharacters(jsonObject);
            int id = getIntValue(jsonObject, "id", 0);
            int total_episodes = getIntValue(jsonObject, "total_episodes", 0);
            long startdate = startdateString.equals("") ? 0 : dateFormat.parse(startdateString).getTime();
            long enddate = enddateString.equals("") ? 0 : dateFormat.parse(enddateString).getTime();

            anime = new Anime(japanese_title, romanji_title, id);
            anime.setImageUrl(image_url);
            anime.setDescription(description);
            anime.setEpisodeTotal(total_episodes);
            anime.setStartDate(startdate);
            anime.setEndDate(enddate);
            anime.setGerneArray(gerneList);
            anime.setAnimeCharacters(animeCharacters);
        } catch (MalformedURLException ex) {
            System.err.println("The api url for requesting anime season data is incorrect.");
        } catch (IOException | HttpResponseException ex) {
            System.err.println("An error has occured while establishing the connection, the following error message was given: ");
            System.err.println(ex.getMessage());
        } catch (ParseException ex) {
            System.err.println("An error has occured while parsing a date, the following error message was given: ");
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (webInput != null) webInput.close();
                if (fileInput != null) fileInput.close();
            } catch (Exception ignored) {
                Log.e(TAG, "Failed to close one or more streams.");
            }
        }

        return anime;
    }

    private HttpURLConnection openConnection(URL url, String requestMethod) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("User-Agent", "PersonalAnimeCalender");
        conn.setRequestMethod(requestMethod);
        return conn;
    }

    private JsonValue getJsonResponse(InputStream inputStream) throws IOException, HttpResponseException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        return Json.parse(in);
    }

    private String getStringValue(JsonValue jsonValue, String jsonValueName, String defaultValue) {
        JsonValue value;
        if (jsonValue.isObject()) {
            value = ((JsonObject) jsonValue).get(jsonValueName);
        } else {
            value = jsonValue;
        }

        if (value != null) {
            if (value.isString()) {
                return value.asString();
            } else {
                Log.i(TAG, "The JsonValue with name: " + jsonValueName + " was empty. The default value: '" + defaultValue + "' was returned instead.");
                return defaultValue;
            }
        } else {
            Log.e(TAG, "The JsonValue with name: " + jsonValueName + " was not found, check if the name of the value is correct. The default value: '" + defaultValue + "' was returned.");
            return defaultValue;
        }
    }

    private int getIntValue(JsonValue jsonValue, String jsonValueName, int defaultValue) {
        JsonValue value;
        if (jsonValue.isObject()) {
            value = ((JsonObject) jsonValue).get(jsonValueName);
        } else {
            value = jsonValue;
        }

        if (value != null) {
            if (value.isNumber()) {
                return value.asInt();
            } else {
                Log.i(TAG, "The JsonValue with name: " + jsonValueName + " was empty. The default value: '" + defaultValue + "' was returned instead.");
                return defaultValue;
            }
        } else {
            Log.e(TAG, "The JsonValue with name: " + jsonValueName + " was not found, check if the name of the value is correct. The default value: '" + defaultValue + "' was returned.");
            return defaultValue;
        }
    }

    private List<String> getGerneList(JsonObject jsonObject) {
        JsonValue jsonValue = jsonObject.get("genres");
        JsonArray jsonArray;
        List<String> stringArray;
        if (jsonValue != null) {
            if (jsonValue.isArray()) {
                jsonArray = jsonValue.asArray();
                int jsonArraySize = jsonArray.size();
                stringArray = new ArrayList<>(jsonArraySize);
                for (int i = 0; i < jsonArraySize; i++) {
                    String gerne = getStringValue(jsonArray.get(i), "", "");
                    Log.i(TAG, "Gerne '" + gerne + "' retrieved.");
                    stringArray.add(gerne);
                }

                return stringArray;
            }
        }

        return new ArrayList<>();
    }

    private List<AnimeCharacter> getCharacters(JsonObject jsonObject) {
        List<AnimeCharacter> animeCharacters;
        JsonArray characterArray = (JsonArray) jsonObject.get("characters");
        if (characterArray == null || !characterArray.isArray()) {
            return new ArrayList<>();
        }

        int size = characterArray.size();
        animeCharacters = new ArrayList<>(size);
        String firstName, lastName, imageUrl;
        int id;
        for (int i = 0; i < size; i++) {
            JsonValue animeCharValue = characterArray.get(i);
            if (animeCharValue.isObject()) {

                JsonObject animeCharObject = animeCharValue.asObject();
                JsonValue value = animeCharObject.get("name_first");
                if (value != null && value.isString()) {
                    firstName = value.asString();
                } else {
                    firstName = "";
                }

                value = animeCharObject.get("name_last");
                if (value != null && value.isString()) {
                    lastName = value.asString();
                } else {
                    lastName = "";
                }

                value = animeCharObject.get("id");
                if (value != null && value.isNumber()) {
                    id = value.asInt();
                } else {
                    id = 0;
                }

                value = animeCharObject.get("image_url_lge");
                if (value != null && value.isString()) {
                    imageUrl = value.asString();
                } else {
                    imageUrl = "";
                }

                Log.i(TAG, "Character '" + lastName + " " + firstName + "' retrieved.");
                AnimeCharacter character = new AnimeCharacter(firstName + " " + lastName, id);
                character.setUrl(imageUrl);
                animeCharacters.add(character);
            } else {
                animeCharacters.add(new AnimeCharacter("", 0));
            }
        }

        return animeCharacters;
    }
}
