package com.example.gregor.animecalender.Utility;

import com.example.gregor.animecalender.Domain.Parameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Gregor on 27-10-2015.
 */
public class URLFactory {

    protected URLFactory() {

    }

    public static URL createURL(String url) throws MalformedURLException {
        return new URL(url);
    }

    public static URL createParameterizedURL(String url, List<Parameter> parameters) throws MalformedURLException {
        StringBuilder parStrBuilder = new StringBuilder().append("?");
        for (Iterator<Parameter> i = parameters.iterator(); i.hasNext();) {
            Parameter par = i.next();
            parStrBuilder.append(par.getName());
            parStrBuilder.append("=");
            parStrBuilder.append(par.getValue());
            if (i.hasNext()) {
                parStrBuilder.append("&");
            }
        }

        return new URL(url + parStrBuilder.toString());
    }
}