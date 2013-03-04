KartMatch
=========

An Android application to randomly assign vehicles to pilots in the context of a kart racing championship.

The primary goal of that application is to randomly associate a pilot with a different car for every race during a Grand Prix, knowing that the pilots are split into several groups and can possibly change group between races. 

To solve that problem, I used a "randomized" version of the Hopcroft-Karp algorithm, which purpose is to find a maximum matching in a bipartite graph. My implementation of that algorithm is here: <a href="https://github.com/pierre-dejoue/kart-match/blob/master/src/fr/neuf/perso/pdejoue/kart_match/HopcroftKarp.java">HopcroftKarp.java</a>

Download the application on my website: <a href="http://pdejoue.perso.neuf.fr/section_stuff/technical/kartmatch.php?lang=EN">Get the app!</a>

<img src="https://github.com/pierre-dejoue/kart-match/blob/master/screenshots/KartMatch_01.jpg?raw=true" />
