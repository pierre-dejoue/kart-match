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
import java.util.Arrays;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.SparseIntArray;

@SuppressLint("UseSparseArrays")
public class CustomApplication extends Application  
{
    private final static String PILOTS_FILE = "pilotes.txt";
    
    private ArrayList<String>  pilot_names = new  ArrayList<String>();              // Image of the internal save file PILOTS_FILE 
    
    public  ArrayList<Integer> car_numbers = new  ArrayList<Integer>();             // Associates the car index with the actual car number, i.e. the set
                                                                                    // V of the bipartite graph passed to the Hopcroft-Karp algorithm.
    
    public  ArrayList<Integer> pilot_group = new  ArrayList<Integer>();             // Associates a pilot to its group. Group number starts at 1
    
    public  int nb_of_pilots      = 0;      // Set by StartActivity.java
    public  int max_nb_of_cars    = 0;      // Set by StartActivity.java
    private int nb_of_groups      = 0;      // Set by PilotsCarsValidateActivity.java
    
    private ArrayList<RaceDetails>                race_history         = null;
    private HashMap<Integer, ArrayList<Integer>>  pilot_preferred_cars = null;      // Bipartite graph that associates each pilot with its preferred cars
                                                                                    // Cars are described by thei car number

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
        // Data structure inits
        //
        race_history         = new ArrayList<RaceDetails>();
        pilot_preferred_cars = new HashMap<Integer, ArrayList<Integer>>();
        
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
    
    public int  getNbOfGroups()
    {
        return nb_of_groups;
    }
    
    public void setNbOfGroups()
    {
        // nb_of_groups must be the minimal integer such that: nb_of_groups * getActualNbOfCars() >= nb_of_pilots
        //
        // This number can be computed directly as follows: nb_of_groups = 1 + (nb_of_pilots-1)/getActualNbOfCars()
        // But it is finally better to obtain indirectly while we initialize the pilot to group mapping array (pilot_group)
        
        nb_of_groups = 1;
        pilot_group.clear();
        
        int pilot_index = 0;
        int car_counter = 0;
       
        while(pilot_index < nb_of_pilots)
        {
            if(car_counter >= getActualNbOfCars())          // Loop on the car_counter if it exceeds the actual number of cars available
            {
                car_counter = 0;
                nb_of_groups++;
            }
            pilot_group.add(nb_of_groups);                  // Pilot 'pilot_index' associated to group 'nb_of_groups'
            car_counter++;                                  // That pilot needs a car
            pilot_index++;                                  // Next pilot
        }
        
        // At the end of the previous loop, nb_of_groups is set to the correct value, i.e. the minimal number of groups that is required
    }
    
    public int getGroupSize(int group_nb)
    {
        if(group_nb < 1 || group_nb > getNbOfGroups())
        {
            return 0;
        }
        int count = 0;
        for(int num : pilot_group)
        {
            if(num == group_nb)
            {
                count++;
            }
        }
        return count;        
    }
    
    public boolean isGroupSizeOK(int group_nb)
    {
        if(group_nb < 1 || group_nb > getNbOfGroups())
        {
            return false;
        } 
        return (getGroupSize(group_nb) <= getActualNbOfCars());               
    }
    
    public boolean allGroupSizesOK()
    {
        // Count number of pilot for all groups
        int[] count_array = new int[getNbOfGroups()];
        Arrays.fill(count_array, 0);
        for(int num : pilot_group)
        {
            count_array[num-1]++;
        }
        
        boolean ret_bool = true;
        
        for(int group_nb = 1; group_nb <= getNbOfGroups(); group_nb++)
        {
            ret_bool &= (count_array[group_nb-1] <= getActualNbOfCars());
        }
        
        return ret_bool;
    }
    
    public ArrayList<String> getRaceHistoryList()
    {
        ArrayList<String>   list = new ArrayList<String>();
        
        for(RaceDetails rd : race_history)
        {
            list.add("Groupe " + Integer.toString(rd.group_nb) + ", course " + Integer.toString(rd.race_nb));
        }
        
        return list;
    }
    
    public int getNextRaceNb(int group_nb)
    {
        int race_nb = 1;
        
        for(RaceDetails rd : race_history)
        {
            if(rd.group_nb == group_nb)
            {
                race_nb++;
            }
        }
        
        return race_nb;
    }
    
    public void reset_race_history()
    {
        race_history.clear();
        pilot_preferred_cars.clear();
        
        for(int pilot_index = 0; pilot_index < nb_of_pilots; pilot_index++)
        {
            ArrayList<Integer> new_car_set = new ArrayList<Integer>();
            pilot_preferred_cars.put(pilot_index, new_car_set);
            for(int car_index = 0; car_index < getActualNbOfCars(); car_index++)
            {
                new_car_set.add(car_numbers.get(car_index));
            }          
        }       
    }
    
    public void save_in_race_history(int group_nb, int race_nb, HopcroftKarp.Result random_matching)
    {
        RaceDetails rd = new RaceDetails();
        
        rd.group_nb             = group_nb;
        rd.race_nb              = race_nb;
        rd.pilot_to_car_mapping = random_matching.clone();
        
        race_history.add(rd);
    }
    
    public RaceDetails get_race_history(int index)
    {
        return race_history.get(index);
    }
    
    // Build a subgraph of a bipartite graph (U,V,E), yet not doing a hard-copy of the inner lists.
    public HashMap<Integer, ArrayList<Integer>> get_subgraph(HashMap<Integer, ArrayList<Integer>> graph, ArrayList<Integer> subset_u)
    {
        HashMap<Integer, ArrayList<Integer>> subgraph = new HashMap<Integer, ArrayList<Integer>>();
       
        for(Integer u :  subset_u)
        {
            if(graph.containsKey(u))
            {
                subgraph.put(u, graph.get(u));
            }
        }
        
        return subgraph;
    }
    
    public HopcroftKarp.Result generate_random_pilot_to_car_mapping(int group_nb)
    {
        ArrayList<Integer> pilot_subset = new ArrayList<Integer>();
        
        for(int index = 0; index < nb_of_pilots; index++)
        {
            if(pilot_group.get(index) == group_nb)
            {
                pilot_subset.add(index);
            }
        }
        
        HashMap<Integer, ArrayList<Integer>> subgraph = get_subgraph(pilot_preferred_cars, pilot_subset);
        
        return HopcroftKarp.findMaximumMatching(subgraph, car_numbers, true);
    }
    
    public void update_pilot_preferred_cars(SparseIntArray used_cars)
    {
        for(int idx = 0; idx < used_cars.size(); idx++)
        {
            int pilot_index = used_cars.keyAt(idx);
            int car_number  = used_cars.valueAt(idx);
            
            ArrayList<Integer> car_list = pilot_preferred_cars.get(pilot_index);
            car_list.remove(car_list.indexOf(car_number));
        }
    }
}
