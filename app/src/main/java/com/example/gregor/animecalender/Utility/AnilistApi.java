package com.example.gregor.animecalender.Utility;

/**
 * Created by Gregor on 27-10-2015.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.text.Html;
import android.util.Log;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Domain.AnimeCharacter;
import com.example.gregor.animecalender.Domain.Parameter;
import com.example.gregor.animecalender.Exceptions.HttpResponseException;

import java.io.BufferedReader;
import java.io.IOException;
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
public class AnilistApi {

    private static final String TAG = "AnilistApi";
    private static final String prefix = "https://anilist.co/api/";

    private String accessToken;
    private String desctription;

    public void getAccessCode() {
        String url = "auth/access_token";
        JsonObject jsonResult;

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter("grant_type", "client_credentials"));
        parameters.add(new Parameter("client_id", "polyanos-6bzkg"));
        parameters.add(new Parameter("client_secret", "t7XXJN3OFZmDvR6m389zBPkwkW"));

        try {
            HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(prefix + url, parameters), "POST");
            jsonResult = (JsonObject) getJsonResponse(conn);

            accessToken = jsonResult.getString("access_token", "");
        } catch (MalformedURLException ex) {
            System.err.println("The api url for requesting the acces token is incorrect.");
        } catch (IOException | HttpResponseException ex) {
            System.err.println("An error has occured while establishing the connection, the following error message was given: ");
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Gets "small anime models" from the anilist api for each anime that adheres to the supplied parameters.
     *
     * @param season
     * @param year
     * @param type
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
            HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(prefix + url, parameters), "GET");
            jsonResult = (JsonArray) getJsonResponse(conn);

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

    public Anime getFullAnimeData(String animeId) {
        String url = "anime/" + animeId + "/characters";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Anime anime = null;

        List<Parameter> parameter = new ArrayList<>();
        parameter.add(new Parameter("access_token", accessToken));

        try {
            HttpURLConnection conn = openConnection(URLFactory.createParameterizedURL(prefix + url, parameter), "GET");
            JsonObject jsonObject = (JsonObject) getJsonResponse(conn);

            String japanese_title = getStringValue(jsonObject, "title_japanese", "");
            String romanji_title = getStringValue(jsonObject, "title_romaji", "");
            String image_url = getStringValue(jsonObject, "image_url_lge", "");
            String description = Html.fromHtml(getStringValue(jsonObject, "description", "")).toString();
            String startdateString = getStringValue(jsonObject, "start_date", "");
            String enddateString = getStringValue(jsonObject, "end_date", "");
            String[] gerneList = getGerneList(jsonObject);
            AnimeCharacter[] animeCharacters = getCharacters(jsonObject);
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
        }

        return anime;
    }

    private HttpURLConnection openConnection(URL url, String requestMethod) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("User-Agent", "PersonalAnimeCalender");
        conn.setRequestMethod(requestMethod);
        return conn;
    }

    private JsonValue getJsonResponse(HttpURLConnection connection) throws IOException, HttpResponseException {
        StringBuilder strBuilder = new StringBuilder();
        String line;

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        if (connection.getResponseCode() == 200) {
            while ((line = in.readLine()) != null) {
                strBuilder.append(line);
            }
            in.close();
            return Json.parse(strBuilder.toString());
        } else {
            throw new HttpResponseException("Bad responsecode recieved. Check if the connection is correctly made and all parameters are correct.");
        }
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

    private String[] getGerneList(JsonObject jsonObject) {
        JsonValue jsonValue = jsonObject.get("genres");
        JsonArray jsonArray;
        String[] stringArray;
        if (jsonValue != null) {
            if (jsonValue.isArray()) {
                jsonArray = jsonValue.asArray();
                int jsonArraySize = jsonArray.size();
                stringArray = new String[jsonArraySize];
                for (int i = 0; i < jsonArraySize; i++) {
                    String gerne = getStringValue(jsonArray.get(i), "", "");
                    Log.i(TAG, "Gerne '" + gerne + "' retrieved.");
                    stringArray[i] = gerne;
                }

                return stringArray;
            }
        }

        return new String[0];
    }

    private AnimeCharacter[] getCharacters(JsonObject jsonObject) {
        AnimeCharacter[] animeCharacters;
        JsonArray characterArray = (JsonArray) jsonObject.get("characters");
        if (characterArray == null || !characterArray.isArray()) {
            return new AnimeCharacter[0];
        }

        int size = characterArray.size();
        animeCharacters = new AnimeCharacter[size];
        String firstName, lastName;
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

                Log.i(TAG, "Character '" + firstName + " " + lastName + "' retrieved.");
                animeCharacters[i] = new AnimeCharacter(firstName, lastName, id);
            } else {
                animeCharacters[i] = new AnimeCharacter("", "", 0);
            }
        }

        return animeCharacters;
    }
}
