package com.nami.aleho;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DateActivity extends Activity {

    //Date, Announcement
    private DateOA[] datoa;
    private List<String> dates = new ArrayList<String>();
    private String[] dow = {"Zondag","Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag"};
    private String[] nom = {"Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"};
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);
        setTitle(getIntent().getStringExtra("subject"));
        config = new Config();
        Gson gson = new Gson();
        Type entityType = new TypeToken< LinkedHashMap<String, String>>(){}.getType();
        LinkedHashMap<String, String> dm = gson.fromJson((String)getIntent().getSerializableExtra("datesMap"), entityType);
        datoa = reConvert(dm);
        new PullTasksThread().start();
    }

    private DateOA[] reConvert(LinkedHashMap<String, String> dates){
        DateOA[] converted = new DateOA[dates.size()];
        int count = 0;
        for (Map.Entry<String, String> entry : dates.entrySet()) {
            converted[count] = new DateOA(entry.getKey(), entry.getValue().substring(entry.getValue().indexOf("/////") + 5) , entry.getValue().substring(0, entry.getValue().indexOf("/////")));
            count++;
        }
        return converted;
    }

    public class PullTasksThread extends Thread {
        public void run () {
            populateDatesList();
        }
    }

    private void populateDatesList() {
        //example: {12 april 2004, Hello World!}
        for(int i=0;i<datoa.length;i++){
            dates.add(datoa[i].getDate());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            //stuff that updates ui
            populateListView();
            registerClickCallback();
            }
        });
    }


    private void populateListView(){
        ArrayAdapter<String> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.lstDates);
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<String>{

        public MyListAdapter(){
            super(DateActivity.this, R.layout.item_view, dates);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }
            int update = getIntent().getIntExtra("update", 0);
            if(position < update )
                itemView.setBackgroundColor(Color.argb(100, 204, 255, 204));
            //Find the date
            String[] currentDate = dates.get(position).split("-");
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imgIcon);
            imageView.setImageResource(R.drawable.anicon);
            TextView textView = (TextView) itemView.findViewById(R.id.txtSubject);
            TextView cat = (TextView) itemView.findViewById(R.id.textCC);
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(currentDate[0]), Integer.parseInt(currentDate[1]), Integer.parseInt(currentDate[2]));
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
            int nameOfMonth = c.get(Calendar.MONTH) - 1;
            
            textView.setText(currentDate[2] + " " + nom[nameOfMonth].toString() + " " + currentDate[0]);
            cat.setText(dow[dayOfWeek].toString());
            notifyDataSetChanged();
            return itemView;
        }
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.lstDates);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                DateOA clickedDate = datoa[position];
                if(!clickedDate.getDate().isEmpty()){
                    Intent announceIntent = new Intent(DateActivity.this, AnnounceActivity.class);
                    announceIntent.putExtra("link", getIntent().getStringExtra("link"));
                    announceIntent.putExtra("announcement", clickedDate.getAnnouncement());
                    announceIntent.putExtra("title", clickedDate.getTitle());
                    DateActivity.this.startActivity(announceIntent);
                    overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.date, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_return) {
            finish();
            overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}