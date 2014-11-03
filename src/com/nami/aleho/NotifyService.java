package com.nami.aleho;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NotifyService extends Service {
    
    String notificationTitle = "ALeho";
    String notificationText = "There is a new announcement!";
    private SharedPreferences subjectPreferences;
    private SharedPreferences.Editor subjectPrefsEditor;
    boolean needsUpdate = false;
    private Alarm alarm = new Alarm();
    
    @Override
    public void onCreate() {
        super.onCreate();
        subjectPreferences = this.getSharedPreferences("subjectPrefs", Context.MODE_PRIVATE);
        alarm.setAlarm(NotifyService.this);
    }
    
    public void setUpdate(String code) throws IOException {
        Document doc = setConnection("http://leho.howest.be/main/announcements/announcements.php?cidReq="+code);
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.select("#sort_area").first();
	        int sizeNow = announceTable.getElementsByClass("announcement").size();
	        subjectPrefsEditor = subjectPreferences.edit();
	        subjectPrefsEditor.putString(code, Integer.toString(sizeNow));
	        subjectPrefsEditor.commit();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm.setAlarm(NotifyService.this);
        new PollTask().execute();
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	alarm.cancelAlarm(NotifyService.this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        
        return null;
    }

    
    private Map<String, String> convertCookies(String[] cookies){
        Map<String, String> converted = new HashMap<String, String>();
        converted.put(cookies[0], cookies[1]);
        return converted;
    }
    private Document setConnection(String path) throws IOException {
        return Jsoup.connect(path).timeout(10 * 1000).cookies(convertCookies(Config.getInstance().getCookiesAsString())).get();
    }
    public boolean checkForUpdate(String code) throws IOException {
    	int needsUpdate = 0;
        Document doc = setConnection("http://leho.howest.be/main/announcements/announcements.php?cidReq="+code);
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.getElementById("sort_area");
	        int sizeNow = announceTable.getElementsByClass("announcement").size();
	        if((Integer.parseInt(subjectPreferences.getString(code, "0")) < sizeNow) || (subjectPreferences.getString(code, "0") == null)){
	            needsUpdate = (sizeNow - Integer.parseInt(subjectPreferences.getString(code, "0")));
	            //setUpdate(code);
	        }
        }
        if(needsUpdate > 0){
        	return true;
        } else {
        	return false;
        }
    }
    
    private class PollTask extends AsyncTask<Void, Void, Void> {
        /**
         * This is where YOU do YOUR work. There's nothing for me to write here
         * you have to fill this in. Make your HTTP request(s) or whatever it is
         * you have to do to get your updates in here, because this is run in a
         * separate thread
         */
        @Override
        protected Void doInBackground(Void... params) {
        	try{
        		for (Map.Entry<String, String> entry : Config.getInstance().getSubjects().entrySet()) {
	                needsUpdate = checkForUpdate(entry.getKey());
	                if(needsUpdate){
	                	notificationText = "Announcement from: " + entry.getValue().split("///")[0].toString();
	                	return null;
	                }
	            }
        	} catch (Exception ex){
        		
        	}
        	return null;
        }
        
        @Override
        protected void onPreExecute()
        {
        	needsUpdate = false;
        }

        
        @Override
        protected void onPostExecute(Void result) {
        	if(needsUpdate){
        		NotificationData notification; //create object
        		notification = new NotificationData(NotifyService.this);
        		notification.SetNotification(R.drawable.ic_launcher, notificationTitle, notificationText, NotifyService.class);
        	}
    		stopSelf();
        }
    }
    
}