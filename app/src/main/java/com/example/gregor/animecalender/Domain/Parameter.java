package com.example.gregor.animecalender.Domain;

/**
 * Created by Gregor on 27-10-2015.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
public class Parameter {

    private String name, value;

    public Parameter(String name, String value) {
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