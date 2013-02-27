/**
 * KartMatch: PilotsCarsValidateActivity.java
 *
 *   Validation screen for the number of pilots, the number of cars and the number of groups. After that screen, those numbers are fixed.
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PilotsCarsValidateActivity extends Activity 
{
    private CustomApplication main_application = null;
 
    //
    // Overridden methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilots_cars_validate);
        main_application = (CustomApplication)getApplication();
        
        // Show the Up button in the action bar.
        setupActionBar();
        
        // Compute the number of groups for the championship based on the number of pilots and number of cars
        main_application.setNbOfGroups();
        
        // Set executive summary text
        TextView summary = (TextView)findViewById(R.id.groups);
        summary.setText(getResources().getString(R.string.final_validate_nb_of_pilots) + " " + Integer.toString(main_application.nb_of_pilots)        + "\n" + 
                        getResources().getString(R.string.final_validate_nb_of_cars)   + " " + Integer.toString(main_application.getActualNbOfCars()) + "\n" + 
                        getResources().getString(R.string.final_validate_nb_of_groups) + " " + Integer.toString(main_application.getNbOfGroups())              
                       );
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() 
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
        {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setTitle(R.string.title_activity_pilots_cars_validate);
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
    
    public void gotoNextActivity(View v)
    {
        // Reset the race history
        main_application.reset_race_history();
        
        // Launch next activity if actual number of cars is different from zero
        Intent intent = new Intent(this, RaceHistoryActivity.class);
        startActivity(intent);
        
        // Do not keep this Activity in memory
        finish();
    }

}
