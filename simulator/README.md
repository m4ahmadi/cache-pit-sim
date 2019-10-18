This simulator will receive as input the following parameters:

1) simulation name which should be "BasicCacheNetwork" to produce the results of the journal file.
2) configuration file path
3) Log setting which can be either "OFF" or "ON"
4) simulation type which varies depending on which figure we are going to produce the results for.

### Configuration file and simulation types:

In the folder "input" next to the source code, you can find different samples of the input files used for different figures. For each simulation type, a subset of parameters must be set which are mentioned in the corresponding input file.

1) input1.txt -> used for figures 3-6. Note that POLICY should be 0, 1, 2, 3 to produce the LRU-model, 2LRU-model, LRU-sim, 2LRU-sim, respectively. Also, the simulation type should be 1,..., 4 to produce Fig.3, ..., Fig.6 results, respectively.

    *  Set Z=1 for IRM and Z=10 for Hyper-10.


2) input2.txt -> used for figures 7-8. Note that POLICY should be 4 and 5 to produce the LRU and 2LRU results, respectively. Also, the simulation type should be 7 and 8 to produce Fig.7 and Fig.8 resutls, respectively.

    *  Set T_ON=86400 for Fig.7a and T_ON=604800 for Fig.7b.
    *  Set C equal to the cache size.


3) input3-sim.txt -> used for simulation values in figures 9-10. Note that POLICY should be 2 and 3 to produce LRU-sim and 2LRU-sim results, respectively.  Also, the simulation type should be 9 and 1 to produce simulation values for Fig.9 and Fig. 10, respectively. 

    * input3-model.txt -> used for analytical values in Fig.10. To produce analytical  results for Fig. 10, the simulation type should be 10. 

 

