package com.nami.aleho;

/**
 * Created by Nami on 12/04/14.
*/

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private static String lehoAnnouncementPath = "http://leho.howest.be/main/announcements/announcements.php?cidReq=";
    private static String lehoCoursePath = "http://leho.howest.be/user_courses.php";

    private Map<String, String> cookies;
    private LinkedHashMap<String, String> subjects;
    private boolean isLoggedIn;

    public Config(){}

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
