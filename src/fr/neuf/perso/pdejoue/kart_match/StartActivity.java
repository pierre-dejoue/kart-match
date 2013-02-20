/**
 * KartMatch: StartActivity.java
 *
 *   First activity visible when loading the application. This is basically where the user enter the number of pilots in the competition,
 *   and the maximum number of cars (which is distinct from the actual number of cars actually used during a race, the maximum number
 *   of cars is more easily understood as the highest car index, knowing that not all cars are in use)
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class StartActivity extends Activity 
{
    private static final int MIN_NB_OF_PILOTS = 1;
    private static final int MAX_NB_OF_PILOTS = 99;
    
    private static final int MIN_NB_OF_CARS = 1;
    private static final int MAX_NB_OF_CARS = 99;
    
    private SharedPreferences settings = null;
    private CustomApplication main_application = null;
    
    private EditText text1;        // Nb of pilots
    private EditText text2;        // Nb of cars

    //
    // Overridden methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        main_application = (CustomApplication)getApplication();
        
        //
        //    Read from Preferences the number of pilots and number of vehicules used during the previous session
        //
        settings = getPreferences(Context.MODE_PRIVATE);
        
        main_application.nb_of_pilots   = settings.getInt("NbOfPilots", 2);
        main_application.max_nb_of_cars = settings.getInt("MaxNbOfCars", 2);
        
        text1 = (EditText)findViewById(R.id.editText1);
        text2 = (EditText)findViewById(R.id.editText2);
        
        text1.setText(Integer.toString(main_application.nb_of_pilots));
        text2.setText(Integer.toString(main_application.max_nb_of_cars));
        
        text1.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus) 
            {
                // When focus is lost check that the text field has valid values.
                if (!hasFocus) 
                {
                    validate_nb_of_pilots(Integer.parseInt(text1.getText().toString()));
                }
            }
        });
        
        text2.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus) 
            {
                // When focus is lost check that the text field has valid values.
                if (!hasFocus) 
                {
                    validate_nb_of_cars(Integer.parseInt(text2.getText().toString()));
                }
            }
        });
    }
    
    //
    // Class specific methods
    //
    
    private void validate_nb_of_pilots(int num)
    {
        // Clip input number
        num = Math.max(MIN_NB_OF_PILOTS, num);
        num = Math.min(                  num, MAX_NB_OF_PILOTS);
        
        // Set the number of pilots
        main_application.nb_of_pilots = num;
           
        // Update the text view accordingly
        text1.setText(Integer.toString(num));
        
        // Write the new value as a preference
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("NbOfPilots", num);
        editor.commit();
    }
    
    public void decNbOfPilots(View v)
    {
        validate_nb_of_pilots(main_application.nb_of_pilots - 1);
    }

    public void incNbOfPilots(View v)
    {
        validate_nb_of_pilots(main_application.nb_of_pilots + 1);
    }
    
    private void validate_nb_of_cars(int num)
    {
        // Clip input number
        num = Math.max(MIN_NB_OF_CARS, num);
        num = Math.min(                  num, MAX_NB_OF_CARS);
        
        // Set the number of pilots
        main_application.max_nb_of_cars = num;
           
        // Update the text view accordingly
        text2.setText(Integer.toString(num));
        
        // Write the new value as a preference
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("MaxNbOfCars", num);
        editor.commit();
    }
    
    public void decMaxNbOfCars(View v)
    {
        validate_nb_of_cars(main_application.max_nb_of_cars - 1);
    }

    public void incMaxNbOfCars(View v)
    {
        validate_nb_of_cars(main_application.max_nb_of_cars + 1);
    }
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }
    
    public void gotoNextActivity(View v)
    {
        // Validate the data in the text fields, in case they are still on focus
        validate_nb_of_pilots(Integer.parseInt(text1.getText().toString()));
        validate_nb_of_cars  (Integer.parseInt(text2.getText().toString()));
        
        // Launch next activity
        Intent intent = new Intent(this, PilotNamesActivity.class);
        startActivity(intent);
    }
    
}
