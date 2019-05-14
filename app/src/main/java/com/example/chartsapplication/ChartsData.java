package com.example.chartsapplication;

import org.json.JSONArray;

public class ChartsData {
    private String dayOrYearOrWeek;

    private String array;

    public ChartsData(String name, String array){
        this.dayOrYearOrWeek = name;
        this.array = array;
    }

    public String getDayOrYearOrWeek() {
        return dayOrYearOrWeek;
    }

    public void setDayOrYearOrWeek(String dayOrYearOrWeek) {
        this.dayOrYearOrWeek = dayOrYearOrWeek;
    }

    public String getArray() {
        return array;
    }

    public void setArray(String array) {
        this.array = array;
    }

}
