package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 10-11-2015.
 */
public class XmlAttribute {
    private String name, value;

    public XmlAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
