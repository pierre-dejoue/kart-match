/**
 * KartMatch: NewRaceFinal.java
 *
 *   Confirmation screen for the new race.
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class NewRaceFinalActivity extends Activity 
{
    private CustomApplication main_application = null;   
    private int               group_nb         = 0;         // The group number for that Activity (group number starts at 1, so 0 is invalid)
    //
    // Overridden methods
    //
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_race_final);
        main_application = (CustomApplication)getApplication();
        
        // Show the Up button in the action bar.
        setupActionBar();
        
        // Get the group number from Intent Extras
        Bundle extras = getIntent().getExtras();
        group_nb = extras.getInt("group_nb");
        
        // Edit the introduction text
        TextView text = (TextView)findViewById(R.id.new_race_intro);
        text.setText("Groupe " + group_nb + ", course " + main_application.getNextRaceNb(group_nb));
        
        // Build the scroll view with all pilots belonging to this group
        build_scroll_view();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
        {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setTitle(R.string.title_activity_new_race_final);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_race_final, menu);
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
    
    private void build_scroll_view()
    {
        // Erase the content of the scroll view
        LinearLayout pilot_list = (LinearLayout)findViewById(R.id.pilot_list);
        pilot_list.removeAllViews();
       
        int car_index = 1;
        for(int index = 0; index < main_application.nb_of_pilots; index++)
        {
            if(main_application.pilot_group.get(index) == group_nb)
            {
                LinearLayout new_horiz_layout = new LinearLayout(this);
                new_horiz_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
                new_horiz_layout.setOrientation(LinearLayout.HORIZONTAL);
                
                EditText new_text_view = new EditText(this);
                new_text_view.setText(main_application.getPilotName(index));
                new_text_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
                new_text_view.setInputType(InputType.TYPE_NULL);                    // The text cannot be edited
                new_horiz_layout.addView(new_text_view);
                
                ImageView new_image_view = new ImageView(this);
                new_image_view.setImageResource(R.drawable.kart);
                new_horiz_layout.addView(new_image_view);
                
                EditText new_text_view_car_id = new EditText(this);
                new_text_view_car_id.setText(Integer.toString(car_index++));        // Dummy car index for now
                new_text_view_car_id.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                new_text_view_car_id.setInputType(InputType.TYPE_NULL);             // The text cannot be edited
                new_horiz_layout.addView(new_text_view_car_id);
                            
                pilot_list.addView(new_horiz_layout);
            }
        }
        
        // Redraw the scroll view
        pilot_list.invalidate();
    }
    
    public void addRaceToHistory(View v)
    {   
        // Add the current race to the history
        main_application.save_in_race_history(group_nb, main_application.getNextRaceNb(group_nb));
        
        // Go back to the History Activity, clearing the two "NewRace" activities
        Intent intent = new Intent(this, RaceHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
