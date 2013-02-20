/**
 * KartMatch: HopcroftKarp.java
 *
 *   Implementation of the Hopcroft-Karp algorithm to find a maximum matching on a bipartite graph.
 *   
 *   This implementation has the option to randomize the order of the edges in the input graph so 
 *   that the returned matching is also random. (I.e. selected randomly among all the possible
 *   solutions for the maximum matching problem).
 *   
 *   Useful information regarding the Hopcroft-Karp algorithm can be found there:
 *      - http://en.wikipedia.org/wiki/Matching_(graph_theory)#Maximum_matchings_in_bipartite_graphs
 *      - http://en.wikipedia.org/wiki/Hopcroft-Karp_algorithm
 *      - http://code.activestate.com/recipes/123641/
 *      
 *   The last link in particular deserves some credits as it is an implementation of the algorithm in
 *   Python done by David Eppstein, which was useful to understand the algorithm and served as a
 *   source of inspiration for this implementation.
 *   
 *   Input:
 *      - The input graph (U, V, E) is described as an HashMap<Integer, ArrayList<Integer>>, mapping each 
 *        vertex in U to a list of vertices in V. All vertexes are Integers, and a non-connected vertex
 *        from U must be associated with the empty list. 
 *      - The set V, as an ArrayList<Integer>. (Only used to compute the unmatched output).
 *      - A boolean is also passed as an argument to specify whether the output should be random or not.
 *      
 *   Output:
 *      - A boolean, true if the matching was perfect, false otherwise
 *      - A maximum matching for that graph, returned as a SparseIntArray, mapping a subset of U to a
 *        subset of V.
 *      - A (possibly random) mapping of the remaining, unmatched, vertices of U to the remaining
 *        vertices of V. This mapping is of course disjoint from the edges of the graph. 
 *   
 *
 * Copyright (C) 2013, Pierre DEJOUE
 * All rights reserved.
 * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
 */
package fr.neuf.perso.pdejoue.kart_match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseIntArray;

@SuppressLint("UseSparseArrays")
public class HopcroftKarp 
{
    // Utility function used to manipulate hash map of type HashMap<Integer, ArrayList<Integer>>
    private static ArrayList<Integer> getValueOrDefault(HashMap<Integer, ArrayList<Integer>> map, Integer key)
    {
        ArrayList<Integer> val = map.get(key);
        if(val == null)
        {
            // Key is not present in the map, create it with the empty list as the default value
            map.put(key, new ArrayList<Integer>());
        }
        
        return map.get(key);
    }
    
    // Recursive function used to build an augmenting path starting from the end node v. 
    // Uses DFS on the U and V layers built during the first phase of the algorithm. 
    // Returns true if an augmenting path was found 
    private static boolean recFindAugmentingPath(Integer v, 
                                                 HashMap<Integer, Integer>            union_all_layers_u, 
                                                 HashMap<Integer, ArrayList<Integer>> union_all_layers_v,
                                                 HashMap<Integer, Integer> matched_v, 
                                                 int k)
    {
        if(union_all_layers_v.containsKey(v))
        {
            for(Integer u: union_all_layers_v.get(v))
            {
                if(union_all_layers_u.containsKey(u))
                {
                    Integer prev_v = union_all_layers_u.get(u);
                    
                    // If the path ending with "prev_v -> u -> v" is an augmenting path
                    if(k == 0 || recFindAugmentingPath(prev_v, union_all_layers_u, union_all_layers_v, matched_v, k-1))
                    {
                        matched_v.put(v, u);                            // Edge u -> v replaces the previous matched edge connected to v.
                        union_all_layers_v.remove(v);                   // Remove vertex v from union_all_layers_v
                        union_all_layers_u.remove(u);                   // Remove vertex u from union_all_layers_u
                        return true;
                    }
                }
            }
        }
        
        return false;   // No augmenting path found
    }
    
    // The Hopcroft-Karp algorithm
    public  static boolean findMaximumMatching(HashMap<Integer, ArrayList<Integer>> in_graph, 
                                               ArrayList<Integer> in_vertices_v, 
                                               boolean randomize, 
                                               SparseIntArray out_matching, 
                                               SparseIntArray out_unmatched)
    {
        // Local variables:
        //
        //
        HashMap<Integer, Integer>            current_layer_u     = new HashMap<Integer, Integer>();
        HashMap<Integer, ArrayList<Integer>> current_layer_v     = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Integer>            union_all_layers_u  = new HashMap<Integer, Integer>();                 // Union of previous layers, except k = 0
        HashMap<Integer, ArrayList<Integer>> union_all_layers_v  = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Integer>            matched_v           = new HashMap<Integer, Integer>();
        HashSet<Integer>                     unmatched_v         = new HashSet<Integer>();
        
        // Clear output arrays
        out_matching.clear();
        out_unmatched.clear();
        
        
        // Loop an finding a minimal augmenting path
        while(true)
        {
            int k = 0;  // U-layers have indexes n = 2*k; V-layers have indexes n = 2*k+1.
            
            // The initial layer of vertices of U is equal to the set of u not in the current matching
            union_all_layers_u.clear();
            current_layer_u.clear();
            for(Integer u : in_graph.keySet())
            {
                if(!matched_v.containsValue(u))
                {
                    current_layer_u.put(u, 0);
                    union_all_layers_u.put(u, 0);
                }
            }

            union_all_layers_v.clear();
            unmatched_v.clear();

            // Use BFS to build alternating U and V layers, in which:
            //  - The edges between U-layer 2*k   and V-layer 2*k+1 are unmatched ones.
            //  - The edges between V-layer 2*k+1 and U-layer 2*k+2 are matched ones.
            
            // While the current layer U is not empty and no unmatched V was encountered
            while(!current_layer_u.isEmpty() && unmatched_v.isEmpty())
            {
                // Build the layer of vertices of V with index n = 2*k+1
                current_layer_v.clear();
                for(Integer u : current_layer_u.keySet())
                {
                    for(Integer v : in_graph.get(u))
                    {
                        if(!union_all_layers_v.containsKey(v))     // If not already in the previous partitions for V
                        {
                            getValueOrDefault(current_layer_v, v).add(u);
                            // Expand of union_all_layers_v is done in the next step, building the U-layer
                        }
                    }
                }
                
                k++;
                // Build the layer of vertices of U with index n = 2*k
                current_layer_u.clear();
                for(Integer v : current_layer_v.keySet())
                {
                    union_all_layers_v.put(v, current_layer_v.get(v));  // Expand the union of all V-layers to include current_v_layer
                    
                    // Is it a matched vertex in V?
                    if(matched_v.containsKey(v))
                    {
                        Integer u = matched_v.get(v);
                        current_layer_u.put(u, v);
                        union_all_layers_u.put(u, v);                   // Expand the union of all U-layers to include current_u_layer
                    }
                    else
                    {
                        // Found one unmatched vertex v. The algorithm will finish the current layer,
                        // then exit the while loop since it has found at least one augmenting path.
                        unmatched_v.add(v);
                    }
                }
            }
            
            // After the inner while loop has completed, either we found at least one augmenting path...
            if(!unmatched_v.isEmpty())
            {
                for(Integer v : unmatched_v)
                {
                    // Use DFS to find one augmenting path ending with vertex V. The vertices from that path, if it 
                    // exists, are removed from the union_all_layers_u and union_all_layers_v maps.
                    if(k >= 1)
                    { 
                        recFindAugmentingPath(v, union_all_layers_u, union_all_layers_v, matched_v, k-1);       // Ignore return status
                    }
                    else
                    {
                        throw new ArithmeticException("k should not be equal to zero here.");
                    }                   
                }
            }
            // ... or we didn't, in which case we already got a maximum matching for that graph
            {
                 break;
            }
        } // end while(true)
        
        // Construct output SparseIntArray out_matching: this is basically the reverse map of map matched_v
        for(Integer v: matched_v.keySet())
        {
            Integer u = matched_v.get(v);
            out_matching.put(u, v);
        }
        
        if(in_graph.size() == in_vertices_v.size() && in_graph.size() == out_matching.size())
        {
            // Perfect matching
            return true;
        }
        else
        {        
            // Construct output out_unmatched
            
            // Get all the unmatched vertices from V. Shuffle them if required
            ArrayList<Integer> remaining_v = new ArrayList<Integer>();
            for(Integer v : in_vertices_v)
            {
                if(!matched_v.containsKey(v))
                {
                    remaining_v.add(v);
                }
            }
            if(randomize)
            {
                Collections.shuffle(remaining_v);
            }
            
            // Associates the unmatched vertices from U with the remaining ones from V until one of those two sets is exhausted
            for(Integer u: in_graph.keySet())
            {
                if(out_matching.indexOfKey(u) < 0)      // If u is not a matched vertex
                {
                    if(!remaining_v.isEmpty())
                    {
                        out_unmatched.put(u, remaining_v.get(0));
                        remaining_v.remove(0);
                    }
                    else
                    {
                        break;
                    }
                }
            }
            
            return false;
        }        
    }
    
    
    
    //
    // Test functions (DEBUG ONLY)
    //
    
    public static void GenericTest( HashMap<Integer, ArrayList<Integer>> in_graph, 
                                    ArrayList<Integer> in_vertices_v, 
                                    boolean randomize)
    {       

        Log.d("HopcroftKarp.Test", in_graph.toString());
        
        SparseIntArray out_matching  = new SparseIntArray();
        SparseIntArray out_unmatched = new SparseIntArray();
        
        findMaximumMatching(in_graph, in_vertices_v, false, out_matching, out_unmatched);
        
        Log.d("HopcroftKarp.Test", "out_matching:");
        for(int idx = 0; idx < out_matching.size(); idx++)
        {
            Log.d("HopcroftKarp.Test", out_matching.keyAt(idx) + " -> " + out_matching.valueAt(idx));
        }
        
        Log.d("HopcroftKarp.Test", "out_unmatched:");
        for(int idx = 0; idx < out_unmatched.size(); idx++)
        {
            Log.d("HopcroftKarp.Test", out_unmatched.keyAt(idx) + " -> " + out_unmatched.valueAt(idx));
        }
    }
    
    public static void Test1()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        for(int idx = 0; idx < 5; idx++)
        {
            test_graph.put(idx, new ArrayList<Integer>());
        }
        test_graph.get(0).add(0);
        test_graph.get(0).add(4);
        test_graph.get(1).add(0);
        test_graph.get(1).add(2);
        test_graph.get(2).add(1);
        test_graph.get(2).add(2);
        test_graph.get(2).add(3);
        test_graph.get(3).add(2);
        test_graph.get(4).add(2);
        
        ArrayList<Integer> array_v = new ArrayList<Integer>();
        array_v.add(0);
        array_v.add(1);
        array_v.add(2);
        array_v.add(3);
        array_v.add(4);
        
        GenericTest(test_graph, array_v, false);
    }
    
    public static void Test2()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        for(int idx = 0; idx < 5; idx++)
        {
            test_graph.put(idx, new ArrayList<Integer>());
        }
        //test_graph.get(0).add(1);
        test_graph.get(0).add(4);
        test_graph.get(1).add(2);
        test_graph.get(2).add(3);
        test_graph.get(3).add(1);
        test_graph.get(4).add(0);
        
        ArrayList<Integer> array_v = new ArrayList<Integer>();
        array_v.add(0);
        array_v.add(1);
        array_v.add(2);
        array_v.add(3);
        array_v.add(4);
        
        GenericTest(test_graph, array_v, false);
    }
    
}