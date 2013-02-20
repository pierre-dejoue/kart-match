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

public class RaceDetails 
{
    public int group_nb;        // Starts at 1
    public int race_nb;         // Starts at 1, this is the race number in group 'group_nb'
    
    HopcroftKarp.Result pilot_to_car_mapping = null; 
    
    public int getNbOfPilots()
    {
        return (pilot_to_car_mapping.matching.size() + pilot_to_car_mapping.unmatched.size());
    }
    
    public boolean isTherePilot(int pilot_id)
    {
        return (pilot_to_car_mapping.matching.indexOfKey(pilot_id) >= 0  || pilot_to_car_mapping.unmatched.indexOfKey(pilot_id) >= 0);
    }
}
