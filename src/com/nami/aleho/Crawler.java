package com.nami.aleho;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nami on 14/04/14.
 */


public class Crawler {
    private String[] cookies;
    private Config config = Config.getInstance();
    private SharedPreferences subjectPreferences;
    private SharedPreferences.Editor subjectPrefsEditor;
    private int needsUpdate;

    public void setUpdate(String code) throws IOException {
        Document doc = setConnection(config.getAnnouncePath()+code);
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.select("#sort_area").first();
	        int sizeNow = announceTable.getElementsByClass("announcement").size();
	        subjectPrefsEditor = subjectPreferences.edit();
	        subjectPrefsEditor.putString(code, Integer.toString(sizeNow));
	        subjectPrefsEditor.commit();
        }
    }

    public Crawler(Context ctxt){
        cookies = new String[2];
        subjectPreferences = ctxt.getSharedPreferences("subjectPrefs", Context.MODE_PRIVATE);
        needsUpdate = 0;
    }

    public Config getConfig(){
        return this.config;
    }

    public void setCookies(String[] cookies){
        this.cookies = cookies;
        this.config.setCookies(cookies);
    }

    public String[] getCookies(){
        return this.cookies;
    }

    public LinkedHashMap<String, String> getIDS(String path) throws IOException {
    	Document doc = setConnection(path);
    	LinkedHashMap<String, String> ids = new LinkedHashMap<String, String>();
    	Elements blocks = doc.getElementsByClass("user_course_category");
    	for(int i=0;i<blocks.size();i++){
    		// Fetching Title here
    		String titleBlock = blocks.get(i).getElementsByClass("title").toString();
    		int startI = titleBlock.lastIndexOf(" - ") + 3;
    		int endI = titleBlock.length() - 7;
    		String category = HtmlEscape.unescapeHtml(titleBlock.substring(startI, endI));
    		// Fetching Course ID here
    		Elements links = blocks.get(i).getElementsByClass("course").select("a[href*=leho.howest.be/main/course_home/course_home.php?cidReq=]");
    		for(int j=0;j<links.size();j++){
    			if(links.get(j).toString().contains("CUR")){
	                int from = links.get(j).toString().lastIndexOf("CUR");
	                int to = links.get(j).toString().lastIndexOf("\">");
	                String courseID = links.get(j).toString().substring(from, to);
	                // Fetching Course name here
	                String courseName = HtmlEscape.unescapeHtml(links.get(j).toString().substring(to + 2, links.get(j).toString().length()-4));
	                ids.put(courseID, courseName+"///"+category);
	                //Log.d(courseID, courseName+"///"+category);
    			}
    		}
    	}
    	config.setSubjects(ids);
    	return ids;
    }

    private Map<String, String> convertCookies(String[] cookies){
        Map<String, String> converted = new HashMap<String, String>();
        converted.put(cookies[0], cookies[1]);
        return converted;
    }

    private Document setConnection(String path) throws IOException {
        return Jsoup.connect(path).timeout(10 * 1000).cookies(convertCookies(cookies)).get();
    }

    public int needsUpdate(){
        return this.needsUpdate;
    }
    public void setUpdated(){
        this.needsUpdate = 0;
    }
    public DateOA[] getAnnouncementDates(String code) throws IOException {
        Document doc = setConnection(config.getAnnouncePath()+code);
        DateOA[] dates = new DateOA[0];
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.getElementById("sort_area");
	        Elements announcements = announceTable.getElementsByClass("announcement");
	        int sizeNow = announcements.size();
	        dates = new DateOA[sizeNow];
	        for(int i=0;i<sizeNow;i++){
	        	String t = announcements.get(i).getElementsByClass("invisible").text();
	        	String date = t.substring(t.lastIndexOf(": ") + 2,t.lastIndexOf(": ") + 12);
	            String announcement = announcements.get(i).getElementsByClass("visible").html();
	            String title = announcements.get(i).getElementsByClass("announcement_title").text();
	            dates[i] = new DateOA(date, announcement, title);
	        }
        }
        return dates;
    }

    public void checkForUpdate(String code) throws IOException {
    	if(Constants.DEBUG)
    		subjectPreferences.edit().clear().commit();
        Document doc = setConnection(config.getAnnouncePath()+code);
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.getElementById("sort_area");
	        int sizeNow = announceTable.getElementsByClass("announcement").size();
	        if((Integer.parseInt(subjectPreferences.getString(code, "0")) < sizeNow) || (subjectPreferences.getString(code, "0") == null)){
	            needsUpdate = (sizeNow - Integer.parseInt(subjectPreferences.getString(code, "0")));
	        }
        }
    }
}
