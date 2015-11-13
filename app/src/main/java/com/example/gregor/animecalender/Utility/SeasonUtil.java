package com.example.gregor.animecalender.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gregor on 13-11-2015.
 */
public class SeasonUtil {
    private int minYear, maxYear;
    private Map<Integer, String> seasonMap;

    public SeasonUtil() {
        minYear = 2000;
        maxYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
        seasonMap = new HashMap<>();

        seasonMap.put(1, "Winter");
        seasonMap.put(2, "Winter");
        seasonMap.put(3, "Winter");
        seasonMap.put(4, "Spring");
        seasonMap.put(5, "Spring");
        seasonMap.put(6, "Spring");
        seasonMap.put(7, "Summer");
        seasonMap.put(8, "Summer");
        seasonMap.put(9, "Summer");
        seasonMap.put(10, "Fall");
        seasonMap.put(11, "Fall");
        seasonMap.put(12, "Fall");
    }

    public List<String> getYears(){
        List<String> yearStringList = new ArrayList<>();
        for(int x = minYear; x < maxYear+1; x++){
            yearStringList.add(String.valueOf(x));
        }
        return yearStringList;
    }

    public String getSeasonString(int monthNumber){
        return seasonMap.get(monthNumber);
    }
}
