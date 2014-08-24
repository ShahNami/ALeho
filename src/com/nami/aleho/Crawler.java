package com.nami.aleho;

import android.content.Context;
import android.content.SharedPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nami on 14/04/14.
 */


public class Crawler {
    private String[] cookies;
    private Config config;
    private SharedPreferences subjectPreferences;
    private SharedPreferences.Editor subjectPrefsEditor;
    private int needsUpdate;


    public void setUpdate(String code) throws IOException {
        Document doc = setConnection(config.getAnnouncePath()+code);
        Element announceTable = doc.select("#agenda_list").first();
        int sizeNow = announceTable.getElementsByClass("announcements_datum").size();
        subjectPrefsEditor = subjectPreferences.edit();
        subjectPrefsEditor.putString(code, Integer.toString(sizeNow));
        subjectPrefsEditor.commit();
    }

    public Crawler(Context ctxt){
        config = new Config();
        cookies = new String[2];
        subjectPreferences = ctxt.getSharedPreferences("subjectPrefs", Context.MODE_PRIVATE);
        needsUpdate = 0;
    }

    public Config getConfig(){
        return this.config;
    }

    public void setCookies(String[] cookies){
        this.cookies = cookies;
    }

    public String[] getCookies(){
        return this.cookies;
    }

    public LinkedHashMap<String, String> getIDS(String path) throws IOException {
        Document doc = setConnection(path);
        LinkedHashMap<String, String> ids = new LinkedHashMap<String, String>();
        Elements links = doc.select("a[href~=leho.howest.be/courses/CUR]");
        Elements mainContent = doc.select("ul.user_course_category");
        int count = 0;
        for(int i=0;i<links.size();i++){
            int from = links.get(i).toString().lastIndexOf("CUR");
            int to = links.get(i).toString().lastIndexOf("\">") - 1;
            //14, 15, 16, 17 define category change
            if(i != 0){
                String categoryChange = links.get(i - 1).toString().substring(from, to).substring(13, 17);
                if(!links.get(i).toString().substring(from, to).substring(13, 17).equalsIgnoreCase(categoryChange)){
                    count++;
                }
                //We don't want this now do we?
                if(mainContent.get(count).text().toLowerCase().contains("algemeen")){
                    count++;
                }
            }
            //Java SSD redirection fix
            if(links.get(i).toString().substring(from, to).equalsIgnoreCase("CUR05600791010337001986")){
                ids.put("CUR05600791010336001986", links.get(i).text() + "///" + mainContent.get(count).text().substring(mainContent.get(count).text().lastIndexOf("- ") + 2));
            } else {
                ids.put(links.get(i).toString().substring(from, to), links.get(i).text() + "///" + mainContent.get(count).text().substring(mainContent.get(count).text().lastIndexOf("- ") + 2));
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
        Element announceTable = doc.select("#agenda_list").first();
        int sizeNow = announceTable.getElementsByClass("announcements_datum").size();
        DateOA[] dates = new DateOA[sizeNow];
        for(int i=0;i<sizeNow;i++){
            String date = announceTable.getElementsByClass("announcements_datum").get(i).text().substring(announceTable.getElementsByClass("announcements_datum").get(i).text().indexOf(": ") + 2);
            String announcement = announceTable.getElementsByClass("text").get(i).html();
            String title = announceTable.getElementsByClass("data").get(i).text().substring(0, announceTable.getElementsByClass("data").get(i).text().toLowerCase().indexOf("zichtbaar voor"));
            dates[i] = new DateOA(date, announcement, title);
        }
        return dates;
    }

    public void checkForUpdate(String code) throws IOException {
        Document doc = setConnection(config.getAnnouncePath()+code);
        Element announceTable = doc.select("#agenda_list").first();
        int sizeNow = announceTable.getElementsByClass("announcements_datum").size();
        if((Integer.parseInt(subjectPreferences.getString(code, "0")) < sizeNow) || (subjectPreferences.getString(code, "0") == null)){
            needsUpdate = (sizeNow - Integer.parseInt(subjectPreferences.getString(code, "0")));
        }
    }
}
