/**
 * KartMatch: RaceDetails.java
 *
 *   Utility class used to capture the details of a single race.
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import android.util.SparseIntArray;

public class RaceDetails 
{
    public int group_nb;        // Starts at 1
    public int race_nb;         // Starts at 1, this is the race number in group 'group_nb'
    
    public SparseIntArray car_used_by_pilot = new SparseIntArray(); 
    
    public int getNbOfPilots()
    {
        return car_used_by_pilot.size();
    }
    
    public boolean isTherePilot(int pilot_id)
    {
        return (car_used_by_pilot.indexOfKey(pilot_id) >= 0);
    }
}
