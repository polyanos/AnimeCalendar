package com.example.gregor.animecalender.Utility;

import com.example.gregor.animecalender.Domain.Anime;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregor on 3-11-2015.
 */
public class AnimeTitleHandler extends DefaultHandler {
    boolean isRomajiTitle, isJapTitle;
    String romajiTitle, japTitle;
    Anime anime;
    List<Anime> foundAnime;

    public AnimeTitleHandler() {
        foundAnime = new ArrayList<>(5000);
    }

    /**
     * Receive notification of the end of the document.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end
     * of a document (such as finalising a tree or closing an output
     * file).</p>
     *
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @see ContentHandler#endDocument
     */
    @Override
    public void endDocument() throws SAXException {
        System.out.println("Retrieved " + foundAnime.size() + " anime.");
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @see ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String attrValue;
        switch (qName) {
            case "anime":
                isRomajiTitle = false;
                isJapTitle = false;
                romajiTitle = "";
                japTitle = "";
                break;
            case "title":
                attrValue = attributes.getValue("type");
                switch (attrValue) {
                    case "official":
                        attrValue = attributes.getValue("xml:lang");
                        if (attrValue.equals("ja")) {
                            isJapTitle = true;
                        }
                        break;
                    case "main":
                        attrValue = attributes.getValue("xml:lang");
                        if (attrValue.equals("x-jat")) {
                            isRomajiTitle = true;
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Receive notification of the end of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri       The Namespace URI, or the empty string if the
     *                  element has no Namespace URI or if Namespace
     *                  processing is not being performed.
     * @param localName The local name (without prefix), or the
     *                  empty string if Namespace processing is not being
     *                  performed.
     * @param qName     The qualified name (with prefix), or the
     *                  empty string if qualified names are not available.
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @see ContentHandler#endElement
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("anime")) {
            if (!japTitle.isEmpty() && !romajiTitle.isEmpty()) {
                anime = new Anime(japTitle, romajiTitle);
                foundAnime.add(anime);
            }
        }
    }

    /**
     * Receive notification of character data inside an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of character data
     * (such as adding the data to a node or buffer, or printing it to
     * a file).</p>
     *
     * @param ch     The characters.
     * @param start  The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @throws SAXException Any SAX exception, possibly
     *                      wrapping another exception.
     * @see ContentHandler#characters
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isRomajiTitle) {
            romajiTitle = String.valueOf(ch, start, length);
            isRomajiTitle = false;
        }
        if (isJapTitle) {
            japTitle = String.valueOf(ch, start, length);
            isJapTitle = false;
        }
    }

    public List<Anime> getRetrievedAnime(){
        return foundAnime;
    }
}
