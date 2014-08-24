package com.nami.aleho;

import java.util.Observable;



/**
 * Created by Nami on 11/04/14.
 */
public class Subject extends Observable {
    private String name;
    private DateOA[] dates;
    private int iconID;
    private String code;
    private String link;
    private String cat;
    private int updates;

    public Subject(int updates, String cat, String name, String code, String link, DateOA[] dates, int iconID){
        super();
        this.name = name;
        this.dates = dates;
        this.iconID = iconID;
        this.code = code;
        this.link = link;
        this.cat = cat;
        this.updates = updates;
    }

    public void triggerObservers(){
        this.iconID = R.drawable.nonoti;
        this.updates = 0;
        setChanged();
        notifyObservers();
    }

    public String getCategory(){
        return this.cat;
    }
    public int getIconID(){
        return this.iconID;
    }
    public String getName(){
        return this.name;
    }
    public DateOA[] getDates(){
        return this.dates;
    }
    public void setDates(DateOA[] dates){
        this.dates = dates;
    }
    public String getLink(){
        return this.link;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public int getUpdates() {
        return updates;
    }
    public void setUpdates(int updates) {
        this.updates = updates;
    }
}
