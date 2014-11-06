package com.nami.aleho;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/*
 * TODO:
 * BB10 does not support background services yet! (android runtime)
 * */
public class NotifyService extends IntentService {
    
    String notificationTitle = "ALeho";
    String notificationText = "There is a new announcement!";
    private SharedPreferences subjectPreferences;
    private SharedPreferences.Editor subjectPrefsEditor;
    boolean needsUpdate = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private LinkedHashMap<String, String> subjectMap = new LinkedHashMap<String, String>();
    private String[] cookies = new String[2];
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    		try{
            	if(subjectMap.size() == 0 || subjectMap == null) {
            		Gson gson = new Gson();
            		Type entityType = new TypeToken< LinkedHashMap<String, String>>(){}.getType();
            		subjectMap = gson.fromJson((String)intent.getSerializableExtra("subjectMap"), entityType);
            		cookies = intent.getExtras().getStringArray("cookies");
            	}
	            scheduler.scheduleWithFixedDelay(new Runnable() {
	                @Override
	                public void run() {
	                	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NotifyService");
	        			wl.acquire();
	                	//Log.d("NotifyService", "Running...");
	                	if(isActivityRunning(MainActivity.class)){
	                		//Don't check
	                		//Log.d("NotifyService", "MainActiviy is running");
	                	} else {
	                		//Log.d("NotifyService", "MainActiviy is NOT running");
		                	needsUpdate = false;
		                	try{
		    	            	for (Map.Entry<String, String> entry : subjectMap.entrySet()) {
		    	            		needsUpdate = checkForUpdate(entry.getKey());
		    	            		//Log.d("Checking: ", entry.getValue().split("///")[0].toString());
		    	            		//We've already sent a notification for this subject
		    	            		//if(notificationText.contains(entry.getValue().split("///")[0].toString())){
		    	            		//	needsUpdate = false;
		    	            		//	break;
		    	            		//}
	    			                if(needsUpdate){
	    			                	//Log.d("Notified:", entry.getValue().split("///")[0].toString());
	    			                	//needsUpdate = true;
		    		                	notificationText = "New announcement from: " + entry.getValue().split("///")[0].toString();
		    		                	//break;
		    		                	//if(needsUpdate){
		    		               		 	NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		    		                        long when = System.currentTimeMillis();         // notification time
		    		                        Notification notification = new Notification(R.drawable.ic_launcher, "ALeho", when);
		    		                        notification.defaults |= Notification.DEFAULT_SOUND;
		    		                        notification.flags |= notification.FLAG_AUTO_CANCEL;
		    		                        Intent notificationIntent = new Intent(NotifyService.this, LoginActivity.class);
		    		                        PendingIntent contentIntent = PendingIntent.getActivity(NotifyService.this, 0, notificationIntent , 0);
		    		                        notification.setLatestEventInfo(getApplicationContext(), notificationTitle, notificationText, contentIntent);
		    		                        nm.notify((NOTIF_ID++) % 3, notification); //we don't want to get spammed do we?
		    		                        needsUpdate = false;
		    		                	//}
		    		                }
		    		            }
		                	} catch (Exception e){
		                		//
		                	}
	                	}
	                	wl.release();
	                }
	            }, 0, 300, TimeUnit.SECONDS); //5 minutes (check 12 times/hour) //Do the cookies expire?
	            
    		} catch (Exception e) {
    			//Log.d("NotifyService", e.getMessage());
    		}
        return START_REDELIVER_INTENT; //keeps intent in memory
    }
    
    protected Boolean isActivityRunning(Class activityClass)
    {
            ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

            for (ActivityManager.RunningTaskInfo task : tasks) {
                if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                    return true;
            }
            return false;
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        scheduler.shutdownNow();
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        subjectPreferences = this.getSharedPreferences("subjectPrefs", Context.MODE_PRIVATE);
    }

    private Map<String, String> convertCookies(String[] cookies){
        Map<String, String> converted = new HashMap<String, String>();
        converted.put(cookies[0], cookies[1]);
        return converted;
    }
    
    private boolean login(String username, String password) throws InterruptedException
    {
        try
        {
            Connection.Response res = Jsoup.connect("https://leho.howest.be/secure/index.php").data(new String[] { "login", username, "password", password, "submitAuth", "OK", "_qf__formLogin", "" }).method(Connection.Method.POST).execute();
            if (res.parse().select("#login_fail").size() > 0) {
            	return false;
            } else {
            	for (Map.Entry<String, String> cookie : res.cookies().entrySet()) {
            		cookies = new String[]{cookie.getKey(), cookie.getValue()};
                }
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }
    
    private Document setConnection(String path) throws IOException {
    	/*if(cookies == null || cookies.length == 0){
    		try {
				login("", "");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    	}*/
        return Jsoup.connect(path).timeout(10 * 1000).cookies(convertCookies(cookies)).get();
    }
    
    private void clearAllPrefs(){
    	subjectPreferences.edit().clear().commit();
    }
    
    private void setNotified(String code, int size){
    	subjectPrefsEditor = subjectPreferences.edit();
        subjectPrefsEditor.putInt(code+":notified", size);
        subjectPrefsEditor.commit();
    }
    
    public boolean checkForUpdate(String code) throws IOException {
    	if(Constants.DEBUG)
    		subjectPreferences.edit().clear().commit();
    	int needsUpdate = 0;
        Document doc = setConnection("http://leho.howest.be/main/announcements/announcements.php?cidReq="+code);
        if(doc.toString().contains("sort_area")){
	        Element announceTable = doc.getElementById("sort_area");
	        int sizeNow = announceTable.getElementsByClass("announcement").size();
	        if((Integer.parseInt(subjectPreferences.getString(code, "0")) < sizeNow) || (subjectPreferences.getString(code, "0") == null)){
	        	needsUpdate = (sizeNow - Integer.parseInt(subjectPreferences.getString(code, "0")));
	            if(needsUpdate > subjectPreferences.getInt(code+":notified", 0)) {
	            	setNotified(code, needsUpdate); //Will be 0 or higher
	            	return true;
	            }
	        }
        }
        return false;
    }
    
    private static int NOTIF_ID = 1;

    public NotifyService(){
        super("NotifyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

	}
    
}