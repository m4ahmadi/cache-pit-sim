package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.server.*;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Statistic;
import edu.sharif.ce.mahmadi.utility.Zipf;

import java.io.FileWriter;
import java.io.IOException;

public abstract class AnalyticCache {


    protected Dist popularityDist; // popularity distribution
    protected double theoreticalDownTime;
    private int Id;


    public AnalyticCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist) {
        popularityDist = mpopularityDist;
        theoreticalDownTime = downloadRate != 0.0 ? (1 / downloadRate) : 0.0;
        this.Id = id;
    }

    public abstract double computeAnalyticHitProbability(int fId);

    public abstract double computeAnalyticForwardingRate(int fId);

    public abstract double computeAnalyticPitHitProbability(int fId);

    public abstract double computeAnalyticResponseTime(int fId);


    public abstract double getCTime();

}
