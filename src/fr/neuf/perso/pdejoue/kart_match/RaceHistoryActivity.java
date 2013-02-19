/**
 * KartMatch: RaceHistoryActivity.java
 *
 *   Screen that lists all the previous races, and allow to access detailled information on them.
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RaceHistoryActivity extends Activity 
{
    private CustomApplication main_application = null;
    private ListView          race_list = null;
 
    //
    // Listeners
    //
    
    OnItemClickListener race_loader = new OnItemClickListener() 
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        {
            Intent intent = new Intent(getApplicationContext(), RaceHistorySingleViewActivity.class);
            intent.putExtra("race_id", position);
            startActivity(intent);
        }
   }; 

   //
   // Overridden methods
   //

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_history);
        main_application = (CustomApplication)getApplication();
        
        // List view in this activity
        race_list = (ListView)findViewById(R.id.race_list);
        
        // Show the Up button in the action bar.
        setupActionBar();
    }
    
    @Override
    protected void onResume() 
    {    
        super.onResume();
        
        //race_list.removeAllViews();
        List<String> race_strings = main_application.getRaceHistoryList();
        
        ArrayAdapter<String> race_list_adapter = new ArrayAdapter<String>(this, R.layout.race_list_view, R.id.text1, race_strings);
        
        race_list.setAdapter(race_list_adapter);
        race_list.setOnItemClickListener(race_loader); 
        
        // In case the history is empty, edit the introduction text accordingly
        if(race_strings.isEmpty())
        {
            TextView text = (TextView)findViewById(R.id.history_introduction);
            text.setText(R.string.empty_history);
        }
    }
    
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
        {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setTitle(R.string.title_activity_race_history);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.race_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) 
        {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void newRaceActivity(View v)
    {
        // Launch next activity
        Intent intent = new Intent(this, NewRaceAllGroupsActivity.class);
        startActivity(intent);
    }
}
