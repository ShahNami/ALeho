package com.nami.aleho;

/**
 * Created by Nami on 12/04/14.
*/

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Application;

public class Config{
    private static String lehoAnnouncementPath = "http://leho.howest.be/main/announcements/announcements.php?cidReq=";
    private static String lehoCoursePath = "http://leho.howest.be/index.php";

    private Map<String, String> cookies;
    private LinkedHashMap<String, String> subjects;
    private boolean isLoggedIn;

    private static Config instance = null;
    protected Config() {
       // Exists only to defeat instantiation.
    }
    public static Config getInstance() {
       if(instance == null) {
          instance = new Config();
       }
       return instance;
    }

    public String getCoursePath(){
        return lehoCoursePath;
    }
    public String getAnnouncePath(){
        return lehoAnnouncementPath;
    }
    public Map<String, String> getCookies()
    {
        return this.cookies;
    }
    public String[] getCookiesAsString(){
    	String[] c = new String[2];
    	for (Map.Entry<String, String> entry : this.cookies.entrySet()) {
    		c[0] = entry.getKey();
    		c[1] = entry.getValue();
        }
    	return c;
    }
    public void setCookies(Map<String, String> cookies)
    {
        this.cookies = cookies;
    }
    public void setSubjects(LinkedHashMap<String, String> subj){
        this.subjects= subj;
    }
    public void setCookies(String[] cookie){
        Map<String, String> c = new HashMap<String, String>();
        c.put(cookie[0], cookie[1]);
        this.cookies = c;
    }
    public LinkedHashMap<String, String> getSubjects(){
        return this.subjects;
    }

    public void setLoggedIn(boolean logged){
        this.isLoggedIn = logged;
    }
    public boolean isLoggedIn(){
        return isLoggedIn;
    }
}
