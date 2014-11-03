package com.nami.aleho;

import java.util.LinkedHashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class Alarm extends BroadcastReceiver 
{    
    
     @Override
     public void onReceive(Context context, Intent intent) 
     {   
         PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         Intent startServiceIntent = new Intent(context, NotifyService.class);
         context.startService(startServiceIntent);
         //Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT).show();
         wl.release();
     }

     public void setSubjects(LinkedHashMap<String, String> s){
    	 Config.getInstance().setSubjects(s);
     }
     public void setCookies(String[] c){
    	 Config.getInstance().setCookies(c);
     }
     
	 public void setAlarm(Context context)
	 {
	     AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	     Intent i = new Intent(context, Alarm.class);
	     PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
	     am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 15, pi); // Millisec * Second * Minute
	 }
	
	 public void cancelAlarm(Context context)
	 {
	     Intent intent = new Intent(context, Alarm.class);
	     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(sender);
	 }
}