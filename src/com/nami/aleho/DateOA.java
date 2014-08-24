package com.nami.aleho;

/**
 * Created by Nami on 11/04/14.
 */
public class DateOA {
    private String date;
    private String announcement;
    private String title;

    public DateOA(String date, String announcement, String title){
        super();
        this.date = date;
        this.announcement = announcement;
        this.title = title;
    }

    public String getDate(){
        return this.date;
    }
    public String getAnnouncement(){
        return this.announcement;
    }
    public String getTitle(){
        return this.title;
    }
}
