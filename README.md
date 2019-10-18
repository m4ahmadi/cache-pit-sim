# cache-pit-sim

This repository contains the simulation and analysis code to reproduce the resutls in the article [Impact of Traffic Characteristics on Request Aggregation in an NDN Router
](https://arxiv.org/abs/1903.06419). 

## Appendix

The appendix folder contains the python script used to compare the analysis in our work with state of the art.

Run the python file with 3 input parameters as:
1.  policy:
    - 0: Method-A
    - 2: Method-B

2. Traffic 
    - 1: IRM
    - 10: hyper-10

3. Download delay (s)

The output will be printed to a text file as (K, hit, time, T_C, C).

## Simulator

The simulator folder is a discrete event simulator for the proposed cache model. The detailed guidelines to re-produce the results for each figure in the article is available in the README file next to the simulator code.

 
