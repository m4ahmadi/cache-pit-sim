package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.caches.AnalyticCache;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

public class AnalyticLFUCache extends AnalyticCache {

    private final int cacheLimit;

    public AnalyticLFUCache(int id, double downloadRate, Simulation simulation, Dist popularityDist, int cacheSize) {
        super(id,downloadRate, simulation, popularityDist);
        cacheLimit = cacheSize;
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        return (fId <= cacheLimit) ? 1.0 : 0.0;
    }


    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        if(fId <= cacheLimit){
            return 0;
        }else{
            return (rate/(rate* theoreticalDownTime+1));
        }
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        if(fId <= cacheLimit){
            return 0;
        }else{
            return (rate*theoreticalDownTime)/(rate*theoreticalDownTime+1);
        }
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        return 0.0;
    }

    @Override
    public double getCTime() {
        return 0;
    }
}
