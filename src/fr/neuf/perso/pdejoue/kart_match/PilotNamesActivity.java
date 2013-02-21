/**
 * KartMatch: PilotNamesActivity.java
 *
 *   Activity screen used to rename pilots.
 *   The pilot names are saved in a local files and retrieved the next time the application is launched.
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import java.io.FileNotFoundException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PilotNamesActivity extends Activity 
{
    // Random identifiers for used in the dynamic scrolling list
    public static final int VIEW_ID_EDITTEXT = 33;
    public static final int VIEW_ID_BUTTON   = 42;
    
    private CustomApplication main_application = null;
 
    //
    // Listeners
    //

    // Listener for the delete button next to the pilot name
    private View.OnClickListener pilot_name_delete_handler = new View.OnClickListener() 
    {
        public void onClick(View v) 
        {
            PilotTag tag = (PilotTag)v.getTag();
            try {
                main_application.deletePilotName(tag.index);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();    
            }
            
            rebuild_scroll_view();            
        }
    };   
        
    // Listener for the pilot name's EditView
    private View.OnFocusChangeListener pilot_name_edition_handler = new View.OnFocusChangeListener()
    {
        @Override
        public void onFocusChange(View v, boolean hasFocus) 
        {
            // When focus is lost check that the text field has valid values.
            if (!hasFocus) 
            {
                TextView text = (TextView)v;
                PilotTag tag = (PilotTag)v.getTag();
                
                try {
                    // Attempt to set the pilot's name according to the text input
                    main_application.setPilotName(tag.index, text.getText().toString());
                    
                    // The name set by the function above might differ from the text input
                    text.setText(main_application.getPilotName(tag.index));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                if(main_application.customPilotName(tag.index))
                {
                    LinearLayout name_list   = (LinearLayout)findViewById(R.id.name_list);
                    LinearLayout line_layout = (LinearLayout)name_list.getChildAt(tag.index);
                    if(line_layout != null && line_layout.findViewById(VIEW_ID_BUTTON) == null)
                    {
                        // If the pilot name is custom, and there is no button associated with this line, add it
                        add_delete_button(line_layout, tag);
                    }
                }
            }           
        }
    };
  
    //
    // Overridden methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot_names);
        main_application = (CustomApplication)getApplication();
        
        // Setup action bar.
        setupActionBar();
        
        // Edit introduction text
        TextView text1 = (TextView)findViewById(R.id.text1);        
        text1.setText("Vous avez demandé " + Integer.toString(main_application.nb_of_pilots) + " pilotes.\nVous pouvez éditer leurs noms (ou pas) :");
        
        // List of pilot names:
        rebuild_scroll_view();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setTitle(R.string.title_activity_pilot_names);
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
  
    
    //
    // Class specific methods
    //
       
    private void add_delete_button(LinearLayout linear_layout, PilotTag pilot_tag)
    {
        Button new_button = new Button(this);
        new_button.setText("Effacer");
        new_button.setOnClickListener(pilot_name_delete_handler);
        new_button.setTag((Object)pilot_tag);
        new_button.setId(VIEW_ID_BUTTON);       //  set ID for latter retrieval
        linear_layout.addView(new_button);
    }
    
    private void rebuild_scroll_view()
    {
        // Erase the content of the scroll view
        LinearLayout name_list = (LinearLayout)findViewById(R.id.name_list);
        name_list.removeAllViews();
       
        for(int index = 0; index < main_application.nb_of_pilots; index++)
        {
            PilotTag pilot_tag = new PilotTag(index);       // Tag attached to the EditText and Button views
            
            LinearLayout new_horiz_layout = new LinearLayout(this);
            new_horiz_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
            new_horiz_layout.setOrientation(LinearLayout.HORIZONTAL);
            
            EditText new_text_view = new EditText(this);
            new_text_view.setText(main_application.getPilotName(index));
            new_text_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
            new_text_view.setTag((Object)pilot_tag);
            new_text_view.setOnFocusChangeListener(pilot_name_edition_handler);  
            new_text_view.setInputType(InputType.TYPE_CLASS_TEXT);
            new_horiz_layout.addView(new_text_view);
                     
            if(main_application.customPilotName(index))
            {
                add_delete_button(new_horiz_layout, pilot_tag);
            }

            
            name_list.addView(new_horiz_layout);
        }
        
        // Redraw the scroll view
        name_list.invalidate();
    }
    
    public void gotoNextActivity(View v)
    {
        // Rebuild scroll view
        // The goal here is to remove the focus on all text field, so that the latest name update is taken into account if need be
        rebuild_scroll_view();
        
        // Launch next activity
        Intent intent = new Intent(this, CarsSelectionActivity.class);
        startActivity(intent);
        
        // Do not keep this Activity in memory
        finish();
    }

}
