/**
 * KartMatch: HopcroftKarp.java
 *
 *   Implementation of the Hopcroft-Karp algorithm to find a maximum matching on a bipartite graph.
 *   
 *   This implementation has the option to randomize the output matching (i.e. it is selected
 *   randomly among all the possible solutions to the maximum matching problem).
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
 *        from U must be associated with the empty list. Sets U and V can be of different sizes.
 *      - The set V, as an ArrayList<Integer>. (Only used to compute the unmatched output).
 *      - A boolean is also passed as an argument to specify whether the output should be random or not.
 *      
 *   Output:
 *      - A boolean, true if the matching was perfect, false otherwise
 *      - A maximum matching for that graph, returned as a SparseIntArray, mapping a subset of U to a
 *        subset of V.
 *      - A mapping of the remaining, unmatched, vertices of U to the remaining vertices of V.
 *        This mapping is of course disjoint from the edges of the graph. 
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

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseIntArray;

@SuppressLint("UseSparseArrays")
public class HopcroftKarp 
{
    public static class Result
    {
        public boolean         perfect_matching;
        public SparseIntArray  matching   = new SparseIntArray();
        public SparseIntArray  unmatched  = new SparseIntArray();
        
        public Result clone()
        {
            Result copy = new Result();
            
            copy.perfect_matching = perfect_matching;
            copy.matching         = matching.clone();
            copy.unmatched        = unmatched.clone();
            
            return copy;
        }
    };
    
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
    
    // The Hopcroft-Karp algorithm
    public  static Result findMaximumMatching(HashMap<Integer, ArrayList<Integer>> graph, 
                                              ArrayList<Integer>                   in_vertices_v, 
                                              boolean                              randomize)
    {
        // Local variables:
        //
        //
        HashMap<Integer, Integer>            current_layer_u     = new HashMap<Integer, Integer>();
        HashMap<Integer, ArrayList<Integer>> current_layer_v     = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Integer>            all_layers_u        = new HashMap<Integer, Integer>();                 // Union of all layers U, except k = 0
        HashMap<Integer, ArrayList<Integer>> all_layers_v        = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Integer>            matched_v           = new HashMap<Integer, Integer>();
        ArrayList<Integer>                   unmatched_v         = new ArrayList<Integer>();
        
        //Log.d("HopcroftKarp.Algo", "graph: " +          graph.toString());
        //Log.d("HopcroftKarp.Algo", "in_vertices_v: " +  in_vertices_v.toString());
        
        // Loop an finding a minimal augmenting path
        while(true)
        {
            int k = 0;  // U-layers have indexes n = 2*k; V-layers have indexes n = 2*k+1.
            
            //Log.d("HopcroftKarp.Algo", "matched_v: " +  matched_v.toString());
            
            // The initial layer of vertices of U is equal to the set of u not in the current matching
            all_layers_u.clear();
            current_layer_u.clear();
            for(Integer u : graph.keySet())
            {
                if(!matched_v.containsValue(u))
                {
                    current_layer_u.put(u, 0);
                    all_layers_u.put(u, 0);
                }
            }

            all_layers_v.clear();
            unmatched_v.clear();

            // Use BFS to build alternating U and V layers, in which:
            //  - The edges between U-layer 2*k   and V-layer 2*k+1 are unmatched ones.
            //  - The edges between V-layer 2*k+1 and U-layer 2*k+2 are matched ones.
            
            // While the current layer U is not empty and no unmatched V was encountered
            while(!current_layer_u.isEmpty() && unmatched_v.isEmpty())
            {
                //Log.d("HopcroftKarp.Algo", "current_layer_u: " + current_layer_u.toString());
                
                // Build the layer of vertices of V with index n = 2*k+1                
                current_layer_v.clear();
                for(Integer u : current_layer_u.keySet())
                {
                    for(Integer v : graph.get(u))
                    {
                        if(!all_layers_v.containsKey(v))     // If not already in the previous partitions for V
                        {
                            getValueOrDefault(current_layer_v, v).add(u);
                            // Expand of all_layers_v is done in the next step, building the U-layer
                        }
                    }
                }
                
                //Log.d("HopcroftKarp.Algo", "current_layer_v: " + current_layer_v.toString());
                
                k++;
                // Build the layer of vertices of U with index n = 2*k
                current_layer_u.clear();
                for(Integer v : current_layer_v.keySet())
                {
                    all_layers_v.put(v, current_layer_v.get(v));  // Expand the union of all V-layers to include current_v_layer
                    
                    // Is it a matched vertex in V?
                    if(matched_v.containsKey(v))
                    {
                        Integer u = matched_v.get(v);
                        current_layer_u.put(u, v);
                        all_layers_u.put(u, v);                   // Expand the union of all U-layers to include current_u_layer
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
                if(randomize)
                {
                    Collections.shuffle(unmatched_v);       // Important to randomize the list here
                                                            // especially in the case where |V| > |U|
                }
                for(Integer v : unmatched_v)
                {
                    // Use DFS to find one augmenting path ending with vertex V. The vertices from that path, if it 
                    // exists, are removed from the all_layers_u and all_layers_v maps.
                    if(k >= 1)
                    { 
                        recFindAugmentingPath(v, all_layers_u, all_layers_v, matched_v, randomize, (k-1));       // Ignore return status
                    }
                    else
                    {
                        throw new ArithmeticException("k should not be equal to zero here.");
                    }                   
                }
            }            
            // ... or we didn't, in which case we already got a maximum matching for that graph
            else
            {
                 break;
            }
        } // end while(true)
        
        
        // Create output class
        Result result = new Result();
        
        result.perfect_matching = (graph.size() == in_vertices_v.size() && graph.size() == matched_v.size());
        result.matching         = get_reverse_mapping(matched_v);
        result.unmatched        = build_unmatched_set(graph, matched_v, in_vertices_v, randomize);       
        
        return result;       
    }
    
    // Recursive function used to build an augmenting path starting from the end node v. 
    // Uses DFS on the U and V layers built during the first phase of the algorithm.
    // This is by the way this function which is responsible for most of the 
    // randomization of the output.
    // Returns true if an augmenting path was found.
    private static boolean recFindAugmentingPath(Integer v, 
                                                 HashMap<Integer, Integer>            all_layers_u, 
                                                 HashMap<Integer, ArrayList<Integer>> all_layers_v,
                                                 HashMap<Integer, Integer>            matched_v, 
                                                 boolean randomize,
                                                 int k)
    {
        if(all_layers_v.containsKey(v))
        {
            ArrayList<Integer> list_u = all_layers_v.get(v);
            
            // If random output is requested
            if(randomize)
            {
                Collections.shuffle(list_u);
            }
            
            for(Integer u: list_u)
            {
                if(all_layers_u.containsKey(u))
                {
                    Integer prev_v = all_layers_u.get(u);
                    
                    // If the path ending with "prev_v -> u -> v" is an augmenting path
                    if(k == 0 || recFindAugmentingPath(prev_v, all_layers_u, all_layers_v, matched_v, randomize, (k-1)))
                    {
                        matched_v.put(v, u);                        // Edge u -> v replaces the previous matched edge connected to v.
                        all_layers_v.remove(v);                     // Remove vertex v from all_layers_v
                        all_layers_u.remove(u);                     // Remove vertex u from all_layers_u
                        return true;
                    }
                }
            }
        }
        
        return false;   // No augmenting path found
    }
    
    // Given an input associative array that stores (key, value) pairs, and assuming that all values are unique,
    // the following function return the reverse mapping: (value, key) pairs.
    public static SparseIntArray get_reverse_mapping(HashMap<Integer, Integer> input_map)
    {
        SparseIntArray reversed_map = new SparseIntArray(); 
        
        for(Integer v: input_map.keySet())
        {
            Integer u = input_map.get(v);
            reversed_map.put(u, v);
        }
        
        return reversed_map;
    }
    
    // Associates all unmatched vertices of U with remaning vertices of from V. Shuffle the result if required
    private static SparseIntArray build_unmatched_set(HashMap<Integer, ArrayList<Integer>> graph,
                                                      HashMap<Integer, Integer>            matched_v,
                                                      ArrayList<Integer>                   in_vertices_v,
                                                      boolean                              randomize)
    {
        ArrayList<Integer> remaining_v  = new ArrayList<Integer>();
        SparseIntArray     unmatched    = new SparseIntArray();     
        
        for(Integer v : in_vertices_v)
        {
            if(!matched_v.containsKey(v))
            {
                remaining_v.add(v);
            }
        }
        
        // Randomize if requested
        if(randomize)
        {
            Collections.shuffle(remaining_v);
        }
        
        // Associates the unmatched vertices from U with the remaining ones from V until one of those two sets is exhausted
        for(Integer u: graph.keySet())
        {
            if(!matched_v.containsValue(u))      // If u is not a matched vertex
            {
                if(!remaining_v.isEmpty())
                {
                    unmatched.put(u, remaining_v.get(0));
                    remaining_v.remove(0);
                }
                else
                {
                    break;
                }
            }
        }
        
        return unmatched;
    } 
    
    //
    // Test functions (DEBUG ONLY)
    //
    
    public static void GenericTest( HashMap<Integer, ArrayList<Integer>> graph, 
                                    ArrayList<Integer>                   in_vertices_v, 
                                    boolean                              randomize)
    {       

        Log.d("HopcroftKarp.Test", "graph: " + graph.toString());
        
        Result result = findMaximumMatching(graph, in_vertices_v, randomize);
        
        Log.d("HopcroftKarp.Test", "perfect_matching: " + result.perfect_matching);
        
        Log.d("HopcroftKarp.Test", "out_matching:");
        for(int idx = 0; idx < result.matching.size(); idx++)
        {
            Log.d("HopcroftKarp.Test", result.matching.keyAt(idx) + " -> " + result.matching.valueAt(idx));
        }
        
        Log.d("HopcroftKarp.Test", "out_unmatched:");
        for(int idx = 0; idx < result.unmatched.size(); idx++)
        {
            Log.d("HopcroftKarp.Test", result.unmatched.keyAt(idx) + " -> " + result.unmatched.valueAt(idx));
        }
    }
    
    public static void Test1()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer>                   array_v    = new ArrayList<Integer>();

        for(int idx = 0; idx < 5; idx++)
        {
            array_v.add(idx);
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
               
        GenericTest(test_graph, array_v, false);
    }
    
    public static void Test2()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer>                   array_v    = new ArrayList<Integer>();
        
        for(int idx = 0; idx < 5; idx++)
        {
            array_v.add(idx);
            test_graph.put(idx, new ArrayList<Integer>());
        }
        test_graph.get(0).add(1);
        test_graph.get(0).add(4);
        test_graph.get(1).add(2);
        test_graph.get(1).add(3);
        test_graph.get(2).add(0);
        test_graph.get(2).add(4);
        test_graph.get(3).add(3);
        test_graph.get(3).add(4);
        test_graph.get(4).add(3);
        test_graph.get(4).add(4);
        
        GenericTest(test_graph, array_v, true);
    }

    public static void Test3()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();        
        ArrayList<Integer>                   array_v    = new ArrayList<Integer>();  
        
        for(int idx = 0; idx < 5; idx++)
        {
            array_v.add(idx);
            ArrayList<Integer> new_list = new ArrayList<Integer>();
            test_graph.put(idx, new_list);
            for(int j = 0; j < 5; j++)
            {
                new_list.add(j);
            }
        }
 
        // Test a complete graph
        GenericTest(test_graph, array_v, true);
    }
    
    public static void Test4()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer>                   array_v    = new ArrayList<Integer>();
        
        for(int idx = 0; idx < 5; idx++)
        {
            array_v.add(idx);
            test_graph.put(idx, new ArrayList<Integer>());
        }
        for(int idx = 0; idx < 5; idx++)
        {
            test_graph.get(0).add(idx);
            test_graph.get(4).add(idx);
        }
        test_graph.get(1).add(2);
        test_graph.get(2).add(3);
        test_graph.get(3).add(1);

        GenericTest(test_graph, array_v, true);
    }
    
    public static void Test5()
    {
        HashMap<Integer, ArrayList<Integer>> test_graph = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<Integer>                   array_v    = new ArrayList<Integer>();
        
        for(int idx = 0; idx < 9; idx++)
        {
            array_v.add(idx);
            test_graph.put(idx, new ArrayList<Integer>());
        }
        
        // The following 3 subgraphs can only be matched in a unique way
        test_graph.get(0).add(1);
        test_graph.get(1).add(0);
        
        test_graph.get(2).add(3);
        test_graph.get(3).add(4);
        test_graph.get(4).add(2);
        
        test_graph.get(5).add(6);
        test_graph.get(6).add(7);
        test_graph.get(7).add(8);
        test_graph.get(8).add(5);
        
        // Add some parasite edges linking those subgraphs together to make the task harder for the algorithm
        // The perfect matching solution for the whole graph is still unique
        test_graph.get(2).add(0);
        test_graph.get(3).add(0);
        test_graph.get(4).add(0);       
        test_graph.get(2).add(1);
        test_graph.get(3).add(1);
        test_graph.get(4).add(1);
        
        test_graph.get(5).add(2);
        test_graph.get(6).add(2);
        test_graph.get(7).add(2);
        test_graph.get(8).add(2);
        test_graph.get(5).add(3);
        test_graph.get(6).add(3);
        test_graph.get(7).add(3);
        test_graph.get(8).add(3);
        test_graph.get(5).add(4);
        test_graph.get(6).add(4);
        test_graph.get(7).add(4);
        test_graph.get(8).add(4);
        
        GenericTest(test_graph, array_v, true);
    }
}
