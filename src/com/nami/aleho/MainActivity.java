package com.nami.aleho;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
/*
 * TODO
 * 
 * */

public class MainActivity extends Activity implements Observer {

    private List<Subject> subjects = new ArrayList<Subject>();
    Crawler crawler;
    private ListView list;
    ProgressDialog mProgressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ALeho");
        crawler = new Crawler(MainActivity.this.getApplicationContext());
        crawler.setCookies(getIntent().getExtras().getStringArray("cookie"));
        Config.getInstance().setCookies(getIntent().getExtras().getStringArray("cookie"));
        if(crawler.getCookies().length > 0) {
        	new InitiateOp().execute("");
        } else {
            finish();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(loginIntent);
        }
    }
    
    private void notifyService(boolean start){
    	Intent intent = new Intent(MainActivity.this, NotifyService.class);
    	if(start) {
    		Gson gson = new Gson();
    		intent.putExtra("cookies", Config.getInstance().getCookiesAsString());
            intent.putExtra("subjectMap", new String(gson.toJson(Config.getInstance().getSubjects())));
            this.startService(intent);
    	} else {
    		this.stopService(intent);
    	}
    }
    
    public void onResume() {
        super.onResume();
    }
    
    public void onPause(){
    	super.onPause();
    }
    

    public void onDestroy(){
    	super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }

    private void addToSubject(int updates, String cat, String name, String code, DateOA[] date, int r){
        String link = crawler.getConfig().getAnnouncePath()+code;
        Subject s;
        if(name.length() > 23) {
            s = new Subject(updates, cat, name.substring(0, 20) + "...", code, link, date, r);
            subjects.add(s);
        } else {
            s = new Subject(updates, cat, name, code, link, date, r);
            subjects.add(s);
        }
        s.addObserver(this);
    }

    private void populateSubjectsList() {
        //example: {CUR05600791010336001986, Java}
        try {
            LinkedHashMap<String, String> subjectMap = crawler.getIDS(crawler.getConfig().getCoursePath());//getIDS(config.getCoursePath());
            for (Map.Entry<String, String> entry : subjectMap.entrySet()) {
                String cat = entry.getValue().substring(entry.getValue().lastIndexOf("///") + 3);
                //DateOA[] dates = crawler.getAnnouncementDates(entry.getKey()); //Takes very long...
                crawler.checkForUpdate(entry.getKey());
                DateOA[] dates = new DateOA[0];
                if(crawler.needsUpdate() > 0){
                    addToSubject(crawler.needsUpdate(), cat, entry.getValue().substring(0, entry.getValue().lastIndexOf("///")), entry.getKey(), dates, R.drawable.newnoti);
                } else {
                    addToSubject(0, cat, entry.getValue().substring(0, entry.getValue().lastIndexOf("///")), entry.getKey(), dates, R.drawable.nonoti);
                }
                crawler.setUpdated();
            }
        } catch (IOException e){

        }
    }

    private void populateListView(){
        ArrayAdapter<Subject> adapter = new MyListAdapter();
        list = (ListView) findViewById(R.id.lstSubjects);
        list.setAdapter(adapter);
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    private class MyListAdapter extends ArrayAdapter<Subject>{

        public MyListAdapter(){
            super(MainActivity.this, R.layout.item_view, subjects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }

            //Find the subject
            Subject currentSubject = subjects.get(position);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imgIcon);
            imageView.setImageResource(currentSubject.getIconID());
            TextView textView = (TextView) itemView.findViewById(R.id.txtSubject);
            textView.setText(currentSubject.getName());
            TextView textCat = (TextView) itemView.findViewById(R.id.textCC);
            textCat.setText(currentSubject.getCategory());
            //Fill the view
            notifyDataSetChanged();
            return itemView;
        }
    }

    private void registerClickCallback() {
        final ListView list = (ListView) findViewById(R.id.lstSubjects);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Subject clickedSubject = subjects.get(position);
                try {
                    clickedSubject.setDates(new GetDates().execute(clickedSubject.getCode()).get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(clickedSubject.getDates().length > 0){
                    Intent intent = new Intent(MainActivity.this, DateActivity.class);
                    intent.putExtra("update", clickedSubject.getUpdates());
                    intent.putExtra("subject", clickedSubject.getName());
                    intent.putExtra("link", clickedSubject.getLink());
                    Gson gson = new Gson();
                    intent.putExtra("datesMap", new String(gson.toJson(convertToMap(clickedSubject.getDates()))));
                    startActivity(intent);
                    new UpdateOp().execute(clickedSubject.getCode());
                    overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
                    clickedSubject.triggerObservers();
                    list.invalidateViews();
                } else {
                    Toast.makeText(MainActivity.this, "This subject has no announcements.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private LinkedHashMap<String, String> convertToMap(DateOA[] dates){
        LinkedHashMap<String, String> converted = new LinkedHashMap<String, String>();
        for(int i=0;i<dates.length;i++){
            converted.put(dates[i].getDate(), dates[i].getTitle()+"/////"+dates[i].getAnnouncement());
        }
        return converted;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_markAll) {
            Toast.makeText(MainActivity.this, "Updating subjects...", Toast.LENGTH_LONG).show();
            int counter = 0;
            for (Map.Entry<String, String> entry : crawler.getConfig().getSubjects().entrySet()) {
                new UpdateOp().execute(entry.getKey());
                subjects.get(counter).triggerObservers();
                counter++;
            }
            list.invalidateViews();
            return true;
        } else if (id == R.id.action_return) {
            finish();
            overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            return true;
        } else if(id == R.id.action_stopService){
        	notifyService(false);
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
	private class InitiateOp extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... params) {
			populateSubjectsList();
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(MainActivity.this);
			//mProgressDialog.setMessage("Downloading Newer Version");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setTitle("Loading");
			mProgressDialog.setMessage("What's happening now:\n- Fetching the subjects\n- Checking for any announcements\n- Fetching the dates\n- Populating the list");
			mProgressDialog.show();
		}
		
		@Override
		protected void onPostExecute(String result) {
			populateListView();
            registerClickCallback();
            notifyService(true);
			mProgressDialog.dismiss();
		}
	}

    private class UpdateOp extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                crawler.setUpdate(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetDates extends AsyncTask<String, Void, DateOA[]> {
        @Override
        protected DateOA[] doInBackground(String... params)  {
            DateOA[] dates = new DateOA[0]; //Takes very long...
            try {
                dates = crawler.getAnnouncementDates(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dates;
        }
    }
}