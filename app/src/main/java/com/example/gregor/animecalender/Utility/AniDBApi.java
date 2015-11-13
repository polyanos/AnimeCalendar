package com.example.gregor.animecalender.Utility;

import android.content.Context;
import android.util.Log;

import com.example.gregor.animecalender.Domain.Anime;
import com.example.gregor.animecalender.Domain.AnimeCharacter;
import com.example.gregor.animecalender.Domain.FileToLoad;
import com.example.gregor.animecalender.Domain.Parameter;
import com.example.gregor.animecalender.Exceptions.AuthorizeException;
import com.example.gregor.animecalender.Exceptions.HttpResponseException;
import com.example.gregor.animecalender.Utility.Interface.Api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Gregor on 4-11-2015.
 */
public class AniDBApi implements Api {
    public static final String NAME = "AniDB";
    private static final String FOLDER_PREFIX = "anidb";
    private static final String TAG = "AniDBApi";
    private static final String PREFIX = "http://api.anidb.net:9001/httpapi";
    private static final String IMAGE_PREFIX = "http://img7.anidb.net/pics/anime/";
    private static final String CLIENT_ID = "animecalender";
    private static final String CLIENT_VERSION = "1";
    private static final String PROTOCOL_VERSION = "1";

    private Context context;

    public AniDBApi(Context context) {
        this.context = context;
    }

    /**
     * Will authorize the app for usage of the api, if authorization isn't needed then this method should do nothing.
     *
     * @throws AuthorizeException
     */
    @Override
    public void authorizeApi() throws AuthorizeException {
    }

    public List<Anime> loadAnimeTitles() {
        List<Anime> animeList = new ArrayList<>();
        try {
            AnimeTitleHandler xmlParserHandler = new AnimeTitleHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(context.getAssets().open("anime-titles.xml"), xmlParserHandler);

            animeList = xmlParserHandler.getRetrievedAnime();
        } catch (ParserConfigurationException | SAXException ex) {
            Log.e(TAG, "There was an error while loading the parser. The exact error message was: " + ex.getMessage());
        } catch (IOException ex) {
            Log.e(TAG, "The xml file was not found or could not be opened.");
        }

        return animeList;
    }


    /**
     * Gets all anime data belonging to the anime with this id. If the data hasn't been retrieved before it write the data into the local cache.
     * If the data has been retrieved before if will use that cached data.
     * The data will be parsed and put in a Anime object.
     * @param animeId the id of the anime that will be lookud up.
     * @return A filled Anime object.
     */
    public Anime getFullAnimeData(String animeId) {
        List<Parameter> parameterList = new ArrayList<>(20);
        parameterList.addAll(getStaticRequiredParameters());
        parameterList.add(new Parameter("request", "anime"));
        parameterList.add(new Parameter("aid", animeId));

        BufferedInputStream in = null;
        InputStream fileInput = null;
        InputStream webInput = null;
        Anime anime = null;

        try {
            FileCache fileCache = new FileCache(context);
            FileToLoad fileToLoad = new FileToLoad(animeId, fileCache.getStandardXmlDirectory());
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            if ((fileInput = fileCache.loadXmlFile(fileToLoad, this)) != null) {
                in = new BufferedInputStream(fileInput);
            } else {
                URL url = URLFactory.createParameterizedURL(PREFIX, parameterList);
                Log.d(TAG, "Loading anime info from the following url: " + url.getPath());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);

                if (connection.getResponseCode() == 200) {
                    FileToLoad fileToLoad1 = new FileToLoad(animeId, fileCache.getStandardXmlDirectory());
                    webInput = connection.getInputStream();
                    fileCache.saveXmlFile(webInput, fileToLoad1, this);
                    fileInput = fileCache.loadXmlFile(fileToLoad1, this);
                    in = new BufferedInputStream(fileInput);
                    in.mark(connection.getContentLength());
                } else throw new HttpResponseException();
            }

            Document document = documentBuilder.parse(in);
            anime = parseAnimeXmlData(document, animeId);
        } catch (HttpResponseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (webInput != null) webInput.close();
                if (fileInput != null) fileInput.close();
            } catch (IOException ex) {
                Log.e(TAG, "Failed to close the inputstreams");
            }
        }

        return anime;
    }

    private List<Parameter> getStaticRequiredParameters() {
        List<Parameter> parameterList = new ArrayList<>(3);
        parameterList.add(new Parameter("client", CLIENT_ID));
        parameterList.add(new Parameter("clientver", CLIENT_VERSION));
        parameterList.add(new Parameter("protover", PROTOCOL_VERSION));
        return parameterList;
    }

    private Anime parseAnimeXmlData(Document document, String animeId) {
        Log.d(TAG, "Starting parsing and retrieval of the anime data");
        Anime anime = new Anime(Integer.parseInt(animeId));
        getTitles(document, anime);
        getDescription(document, anime);
        getEndDate(document, anime);
        getStartDate(document, anime);
        getEpisodeData(document, anime);
        getImageUrl(document, anime);
        getTags(document, anime);
        getCharacters(document, anime);

        Log.d(TAG, "Finished retrieving data.");
        return anime;
    }

    private void getTitles(Document document, Anime anime) {
        Log.d(TAG, "Beginning retieval of titles.");
        NodeList nodeList = document.getElementsByTagName("titles");
        Element titleList = (Element) nodeList.item(0);
        NodeList titleNodeList = titleList.getChildNodes();

        Log.d(TAG, "Found " + titleNodeList.getLength() + " title tags.");
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            if (titleNodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element titleElement = (Element) titleNodeList.item(i);
            switch (titleElement.getAttribute("xml:lang")) {
                case "ja":
                    if (titleElement.getAttribute("type").equals("official")) {
                        anime.setJapaneseTitle(titleElement.getTextContent());
                        Log.d(TAG, "Found the japanese title: " + titleElement.getTextContent());
                    }
                case "x-jat":
                    if (titleElement.getAttribute("type").equals("main")) {
                        anime.setRomanjiTitle(titleElement.getTextContent());
                        Log.d(TAG, "Found the romanji title: " + titleElement.getTextContent());
                    }
            }
        }
    }

    private void getDescription(Document document, Anime anime) {
        Pattern regexPatterLink = Pattern.compile("http:\\/\\/\\S+\\s\\[([\\w+\\s*]+)\\];?");
        Log.d(TAG, "Beginning retieval of the description.");

        NodeList nodeList = document.getElementsByTagName("description");
        String description = "";
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE && node.getParentNode().getNodeName().equals("anime"))
            {
                description = node.getTextContent();
            }
        }

        Log.d(TAG, "Found the following description: " + description);
        description = regexPatterLink.matcher(description).replaceAll("$1");
        anime.setDescription(description);
    }

    private void getImageUrl(Document document, Anime anime) {
        Log.d(TAG, "Beginning retieval of the image url.");
        String url = "";
        NodeList nodeList = document.getElementsByTagName("picture");
        Element element = (Element) nodeList.item(0);
        url = IMAGE_PREFIX + element.getTextContent();
        Log.d(TAG, "Found the following image url: " + url);
        anime.setImageUrl(url);
    }

    private void getStartDate(Document document, Anime anime) {
        Log.d(TAG, "Beginning retieval of the startdate.");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat alternativeDateFormat = new SimpleDateFormat("yyyy-MM");
        long startDateTimestamp = 0;
        NodeList nodeList = document.getElementsByTagName("startdate");
        Element element = (Element) nodeList.item(0);
        try {
            startDateTimestamp = dateFormat.parse(element.getTextContent()).getTime();

        } catch (ParseException ex) {
            Log.d(TAG, "Using alternative date format.");
            try {
                startDateTimestamp = alternativeDateFormat.parse(element.getTextContent()).getTime();
            } catch (ParseException e) {
                Log.e(TAG, "An invalid date string has been found. Please check if the correct element is being retrieved. The following string was recieved: " + element.getTextContent());
            }
        }
        Log.d(TAG, "Found the following startdate timestamp: " + startDateTimestamp);
        anime.setStartDate(startDateTimestamp);
    }

    private void getEndDate(Document document, Anime anime) {
        Log.d(TAG, "Beginning retieval of the enddate.");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat alternativeDateFormat = new SimpleDateFormat("yyyy-MM");
        long endDateTimestamp = 0;
        NodeList nodeList = document.getElementsByTagName("enddate");
        Element element = (Element) nodeList.item(0);
        try {
            endDateTimestamp = dateFormat.parse(element.getTextContent()).getTime();

        } catch (ParseException ex) {
            Log.d(TAG, "Using alternative date format.");
            try {
                endDateTimestamp = alternativeDateFormat.parse(element.getTextContent()).getTime();
            } catch (ParseException e) {
                Log.e(TAG, "An invalid date string has been found. Please check if the correct element is being retrieved. The following string was recieved: " + element.getTextContent());
            }
        }
        Log.d(TAG, "Found the following enddate timestamp: " + endDateTimestamp);
        anime.setEndDate(endDateTimestamp);
    }

    private void getEpisodeData(Document document, Anime anime) {
        Log.d(TAG, "Beginning retrieval of episode data");
        int episodeTotal = 0;
        NodeList nodeList = document.getElementsByTagName("episodecount");
        Element element = (Element) nodeList.item(0);
        try {
            episodeTotal = Integer.parseInt(element.getTextContent());
        } catch (NumberFormatException ex) {
            Log.e(TAG, "An invalid integer string has been retrieved. Please check if the correct element is being retrieved. The following string was recieved: " + element.getTextContent());
        }
        Log.d(TAG, "Found the folowing episode total: " + episodeTotal);
        anime.setEpisodeTotal(episodeTotal);
    }

    private void getTags(Document document, Anime anime){
        Log.d(TAG, "Beginning retrieval of anime tags");
        List<String> tagList = new ArrayList<>();

        Node tagsNode = document.getElementsByTagName("tags").item(0);
        NodeList tagNodeList = tagsNode.getChildNodes();
        Log.d(TAG, "Found " + tagNodeList.getLength() + " children nodes.");
        for(int i = 0; i < tagNodeList.getLength(); i++){
            Node tagNode = tagNodeList.item(i);
            if(tagNode.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }

            NamedNodeMap tagNodeAttributes = tagNode.getAttributes();
            Node attributeNode;
            if((attributeNode = tagNodeAttributes.getNamedItem("infobox")) != null && attributeNode.getTextContent().equals("true")){
                NodeList tagNodeChildren = tagNode.getChildNodes();
                for(int x = 0; x < tagNodeChildren.getLength(); x++){
                    Node tagNodeChild = tagNodeChildren.item(x);

                    if(tagNodeChild.getNodeType() == Node.ELEMENT_NODE && tagNodeChild.getNodeName().equals("name")){
                        Log.d(TAG, "Found a relevant tag named " + tagNodeChild.getTextContent());
                        tagList.add(tagNodeChild.getTextContent());
                    }
                }
            }
        }
        Log.d(TAG, "Found " + tagList.size() + " relevant tags.");
        anime.setGerneArray(tagList);
    }

    private void getCharacters(Document document, Anime anime){
        Log.d(TAG, "Beginning retrieval of anime characters");
        List<AnimeCharacter> characterList = new ArrayList<>();
        String name = "", url = "";
        int id = 0;

        Node charactersNode = document.getElementsByTagName("characters").item(0);
        if(charactersNode == null){
            Log.e(TAG, "Found no character data, exiting");
            return;
        }
        NodeList characterNodeList = charactersNode.getChildNodes();
        for(int x = 0; x < characterNodeList.getLength(); x++){
            Node characterNode = characterNodeList.item(x);
            if(characterNode.getNodeType() != Node.ELEMENT_NODE ){
                continue;
            } else if(!characterNode.getAttributes().getNamedItem("type").getTextContent().equals("main character in")){
                continue;
            }

            id = Integer.parseInt(characterNode.getAttributes().getNamedItem("id").getTextContent());
            Log.d(TAG, "Found a main character with id: " + id);

            NodeList characterDataNodes = characterNode.getChildNodes();
            for(int y = 0; y < characterDataNodes.getLength(); y++){
                Node characterDataNode;

                if((characterDataNode = characterDataNodes.item(y)).getNodeType() == Node.ELEMENT_NODE){
                    switch(characterDataNode.getNodeName()){
                        case "name":
                            name = characterDataNode.getTextContent();
                            break;
                        case "picture":
                            url = IMAGE_PREFIX + characterDataNode.getTextContent();
                            break;
                    }
                }
            }
            Log.d(TAG, "Name of found character is " + name);

            AnimeCharacter character = new AnimeCharacter(name, id);
            character.setUrl(url);
            characterList.add(character);
        }
        anime.setAnimeCharacters(characterList);
        Log.d(TAG, "Found " + characterList.size() + " characters");
    }

    @Override
    public String getApiDirectory() {
        return FOLDER_PREFIX;
    }
}
