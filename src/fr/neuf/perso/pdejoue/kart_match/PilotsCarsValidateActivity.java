/**
 * KartMatch: PilotsCarsValidateActivity.java
 *
 *   Validation screen for the number of pilots, the number of cars and the number of groups. After that screen, those numbers are fixed.
 *
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
import android.widget.Button;
import android.widget.TextView;

public class PilotsCarsValidateActivity extends Activity 
{
    private CustomApplication main_application = null;
    
    // Max number of groups to avoid overloading the UI
    private static final int MAX_NUMBER_OF_GROUPS = 6;
 
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
        TextView summary_1 = (TextView)findViewById(R.id.pilots_cars);
        summary_1.setText(getResources().getString(R.string.final_validate_nb_of_pilots) + " " + Integer.toString(main_application.nb_of_pilots)        + "\n" + 
                          getResources().getString(R.string.final_validate_nb_of_cars)   + " " + Integer.toString(main_application.getActualNbOfCars())
                         );
        
        
        
        TextView summary_2 = (TextView)findViewById(R.id.groups);
        if(main_application.getNbOfGroups() <= MAX_NUMBER_OF_GROUPS)
        {
            summary_2.setText(getResources().getString(R.string.final_validate_nb_of_groups) + " " + Integer.toString(main_application.getNbOfGroups()));
        }
        else
        {
            summary_2.setText(getResources().getString(R.string.final_validate_nb_of_groups) + " " + Integer.toString(main_application.getNbOfGroups())  + "\n\n\n" + 
                              getResources().getString(R.string.nb_of_groups_warning_1) + " " + MAX_NUMBER_OF_GROUPS + " " + getResources().getString(R.string.nb_of_groups_warning_2));
            summary_2.setTextColor(getResources().getColor(R.color.dark_red));
            
            Button button = (Button)findViewById(R.id.confirm_button);
            button.setTextColor(getResources().getColor(R.color.dark_red));
        }
        
        
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
        if(main_application.getNbOfGroups() <= MAX_NUMBER_OF_GROUPS)
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

}
