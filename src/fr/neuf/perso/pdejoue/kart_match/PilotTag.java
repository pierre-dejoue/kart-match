/**
 * KartMatch: PilotTag.java
 *
 *   Utility structure used as a tag in the various lists displayed by the application
 *   This is a way to retrieve the index of the pilot associated with a list entry
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

public class PilotTag 
{
    int index;
    
    public PilotTag(int index)
    {
        this.index = index;
    }
}
