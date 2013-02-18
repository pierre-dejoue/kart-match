/**
 * KartMatch: CustomApplication.java
 *
 *   The Main Application. It contains all the data common to all activities
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

public class CustomApplication extends Application  
{
    private final static String PILOTS_FILE = "pilotes.txt";
    
    private ArrayList<String>  pilot_names = new  ArrayList<String>();             // Image of the internal save file PILOTS_FILE 
    public  ArrayList<Integer> car_numbers = new  ArrayList<Integer>();            // Associates the car index with the actual car number
    
    public  int nb_of_pilots      = 0;      // Set by StartActivity.java
    public  int max_nb_of_cars    = 0;      // Set by StartActivity.java
    private int nb_of_groups      = 0;      // Set by PilotsCarsValidateActivity.java

    public int getActualNbOfCars()
    {
        return car_numbers.size();
    }
    
    @Override
    public void onCreate() 
    {
        // Always call parent's onCreate
        super.onCreate();
        
        //
        // Initial read of file PILOTS_FILE, if the file does not exist is is created
        //

        FileInputStream file_in = null;
        
        try 
        {
            file_in = openFileInput(PILOTS_FILE);
        } 
        catch (FileNotFoundException e) 
        {
            try 
            {
                // If file does not exist, create it
                openFileOutput(PILOTS_FILE, Context.MODE_PRIVATE);
                file_in = openFileInput(PILOTS_FILE);
    
            } 
            catch (FileNotFoundException e1) 
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }    
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(file_in));
        
        try
        {
            String pilot_name;
            while((pilot_name = reader.readLine()) != null)
            {
                pilot_names.add(pilot_name);
            }
        } 
        catch (IOException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

    public boolean customPilotName(int index)
    {
        return (index < pilot_names.size() && !(pilot_names.get(index).equals("")));
    }
    
    
    // Make sure that the default pilot name pattern is matching in both functions isDefaultPilotName() and defaultPilotName(). Thank You.
    private boolean isDefaultPilotName(String name)
    {
        // Returns true if the passed name has the form of a default name ("Pilote x")
        return name.matches("Pilote\\s\\d{1,}");
    }
    
    private String defaultPilotName(int index)
    {
        return "Pilote " + Integer.toString(index+1);
    }
    
    public String getPilotName(int index)
    {
        // The input index is the logical index. It can exceed the pilot_names arraylist's size, in which case the default naming convention is used
        
        if(!customPilotName(index))
        {
            return defaultPilotName(index);
        }
        else
        {
            return pilot_names.get(index);
        }
    }
    
    public void setPilotName(int index, String name) throws FileNotFoundException
    {
        if(index < 0)
        {
            return;
        }
        
        // Look for the same name already in the list
        int match_index = pilot_names.indexOf(name);
        
        if(isDefaultPilotName(name))                                    // Attempt to set a default name
        {
            name = "";    
        }            
        else if(match_index != -1  && match_index < nb_of_pilots)       // If name is already in the VISIBLE list         
        {
            if(match_index == index)
            {
                return;         // Name already set, early return
            }
            // Ignore the name passed as argument and use the default naming convention instead (simply put the empty string in arraylist pilot_names)
            name = "";            
        }
        else if(match_index != -1)
        {
            deletePilotName(match_index);                               // Remove the pilot name from the list since it is outside of the VISIBLE list of names
        }

        if(name.equals("") && 
           index == (pilot_names.size() - 1))
        {
            // If we erase the last element of the array, erase all the previous elements that are also the empty string
            pilot_names.set(index,  "");
            while(index >= 0 && pilot_names.get(index).equals(""))
            {
                pilot_names.remove(index);
                index--;
            }
        }
        else
        {
            // In case index >= pilot_names.size(), need to pad the array list with empty strings
            int idx = pilot_names.size();
            while(idx <= index)
            {
                pilot_names.add("");        // Add empty string at index 'idx'
                idx++;
            }
        
            // Will not raise IndexOutOfBoundException because the element exists
            pilot_names.set(index,  name);
        }
        
        // Rewrite save file according to the new state of array pilot_names
        regenerate_pilots_file();
        
    }
    
    public void initCarNumbers()
    {
        car_numbers.clear();
        
        // Initialize the ArrayList with the car numbers
        for(int num = 1; num <= max_nb_of_cars; num++)
        {
            car_numbers.add(num);
        }
    }

    public boolean isCarSelected(int car_number)
    {
        return (car_numbers.indexOf(car_number) != -1);
    }
    
    public void unselectCar(int car_number)
    {
        if(car_number >= 1 && car_number <= max_nb_of_cars)
        {
            int match_index;
            if((match_index = car_numbers.indexOf(car_number)) != -1)
            {
                car_numbers.remove(match_index);
            }
        }        
    }
    
    public void selectCar(int car_number)
    {
        if(isCarSelected(car_number))
        {
            return;     // Early return if already in list
        }
        
        if(car_number >= 1 && car_number <= max_nb_of_cars)
        {
            int index = 0;
            while(index < car_numbers.size() && car_numbers.get(index) < car_number)
            {
                index++;
            }
            if(index == car_numbers.size())     // Insert at the end of the list
            {
                car_numbers.add(car_number);
            }
            else                                // Insert in the middle of the list
            {
                car_numbers.add(index, car_number);
            }
        }
    }
    
    
    public void deletePilotName(int index) throws FileNotFoundException
    {
        if(index < 0)
        {
            return;
        }
        
        if(index < (pilot_names.size() - 1))
        {
            pilot_names.remove(index);
        }
        else if(index == (pilot_names.size() - 1))      // Last element
        {
            // If we remove the last element of the array, erase all the previous elements that are also the empty string
            pilot_names.set(index,  "");
            while(index >= 0 && pilot_names.get(index).equals(""))
            {
                pilot_names.remove(index);
                index--;
            }
        }
        
        // Rewrite save file according to the new state of array pilot_names
        regenerate_pilots_file();
    }
    
    public int  getNbOfGroups()
    {
        return nb_of_groups;
    }
    
    public void setNbOfGroups()
    {
        // nb_of_groups must be the minimal integer such that: nb_of_groups * getActualNbOfCars() >= nb_of_pilots
        
        nb_of_groups = 1 + (nb_of_pilots-1)/getActualNbOfCars();
    }
    
    public void regenerate_pilots_file() throws FileNotFoundException
    {
        // Open empty back up file
        PrintWriter writer  = new PrintWriter(openFileOutput(PILOTS_FILE , Context.MODE_PRIVATE));
        
        // Copy list
        for(String s: pilot_names)
        {
            writer.println(s);
        }
        
        // Close file
        writer.close();
    }
}
