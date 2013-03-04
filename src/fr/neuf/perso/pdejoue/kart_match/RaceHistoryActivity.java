/**
 * KartMatch: RaceHistoryActivity.java
 *
 *   Screen that lists all the previous races, and allow to access detailed information on them.
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RaceHistoryActivity extends Activity 
{
    private CustomApplication main_application = null;
    private ListView          race_list = null;
    private List<String>      race_strings = null;
    
    //
    // Custom Adapter class for the ListView
    //
    
    private final static int DELETE_BUTTON_UNIQUE_ID = 789;
    public class CustomArrayAdapter extends ArrayAdapter<String>
    {
        public CustomArrayAdapter(Context context, int resource, int textViewResourceId, List<String> objects) 
        { 
            super(context, resource, textViewResourceId, objects); 
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            LinearLayout last_race  = (LinearLayout)super.getView(position, convertView, parent);
                
            // If this View is the last in the history list, and it does not have a delete button, add it
            if(position == (main_application.getRaceHistorySize()-1) &&
               last_race.findViewById(DELETE_BUTTON_UNIQUE_ID) == null)
            {
                // Add a "delete" button to the last race in the history.  
                // For information about how to add a button to a ListView and still keep it clickable, 
                // read this: http://android.cyrilmottier.com/?p=525
                Button new_button = new Button(getContext());
                new_button.setText(getResources().getString(R.string.del_last_race_button));
                new_button.setOnClickListener(last_race_delete_handler);
                new_button.setId(DELETE_BUTTON_UNIQUE_ID);
                last_race.addView(new_button);
            }
            else if(position != (main_application.getRaceHistorySize()-1) &&
                    last_race.findViewById(DELETE_BUTTON_UNIQUE_ID) != null)
            {
                last_race.removeView(last_race.findViewById(DELETE_BUTTON_UNIQUE_ID));               
            }

            return last_race;
        }
        
    }
    
    //
    // Listeners
    //
    
    // ListView listener
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

   // Listener for the delete button of the last race in the history
   private View.OnClickListener last_race_delete_handler = new View.OnClickListener() 
   {
       public void onClick(View v) 
       {
           AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
           builder.setTitle(R.string.history_delete_dialog_title);
           builder.setCancelable(true);
           builder.setIcon(android.R.drawable.ic_dialog_alert);
           builder.setMessage(R.string.history_delete_dialog_message);
           builder.setPositiveButton(android.R.string.ok,
                       new DialogInterface.OnClickListener() 
                       {
                           public void onClick(DialogInterface dialog, int id) 
                           {
                               main_application.delete_last_race_from_history();                              
                               refresh_list_view();  
                           }
                       });       
           builder.setNegativeButton(android.R.string.cancel,
                       new DialogInterface.OnClickListener() 
                       {
                           public void onClick(DialogInterface dialog, int id) 
                           {
                               dialog.cancel();
                           }
                       });
               
           AlertDialog alert = builder.create();
           alert.show();          
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
        
        // List of races
        race_strings = main_application.getRaceHistoryList();
        
        // Show the Up button in the action bar.
        setupActionBar();
    }
    
    @Override
    protected void onResume() 
    {    
        super.onResume();
          
        build_list_view();       
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
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle item selection
        switch (item.getItemId()) 
        {
            case R.id.about_menu:
                main_application.about_dialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onBackPressed() 
    {
        // Unless the history is empty, open a dialog to confirm the back action
        
        if(main_application.getRaceHistorySize() == 0)
        {
            super.onBackPressed();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.history_dialog_title);
            builder.setCancelable(true);
            builder.setIcon(android.R.drawable.stat_notify_error);
            builder.setMessage(R.string.history_dialog_message);
            builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() 
                        {
                            public void onClick(DialogInterface dialog, int id) 
                            {
                                RaceHistoryActivity.super.onBackPressed();
                            }
                        });       
            builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() 
                        {
                            public void onClick(DialogInterface dialog, int id) 
                            {
                                dialog.cancel();
                            }
                        });
                
            AlertDialog alert = builder.create();
            alert.show();
        }  
    }  
    
    //
    // Class specific methods
    //
    
    public void build_list_view()
    {       
        CustomArrayAdapter race_list_adapter = new CustomArrayAdapter(this, R.layout.race_list_view, R.id.text1, race_strings);
        
        race_list.setAdapter(race_list_adapter);
        race_list.setOnItemClickListener(race_loader); 
        
        // In case the history is empty, edit the introduction text accordingly
        if(race_strings.isEmpty())
        {
            TextView text = (TextView)findViewById(R.id.history_introduction);
            text.setText(R.string.empty_history);
        }
        
    }

    @SuppressWarnings("unchecked")
    public void refresh_list_view()
    {
        // Update list
        String last_race_str = race_strings.get(race_strings.size()-1);  // Name of last race
        race_strings = main_application.getRaceHistoryList();
        
        // Notify the Adapter
        ((ArrayAdapter<String>)race_list.getAdapter()).remove(last_race_str);
    }
    
    public void newRaceActivity(View v)
    {
        // Launch next activity
        Intent intent = new Intent(this, NewRaceAllGroupsActivity.class);
        startActivity(intent);
    }
}
