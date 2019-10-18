package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

public class LFUCache extends BasicCache {

    private final int cacheLimit;

    public LFUCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize) {
        super(id, pit, index, downloadRate, simulation, mpopularityDist);

        this.cacheLimit = mcacheSize;
        analyticCache = new AnalyticLFUCache(id, downloadRate, simulation, mpopularityDist, mcacheSize);
    }

    @Override
    protected void writeMessageInTheCache(int fId) {

    }

    @Override
    protected boolean isInTheCache(int fId) {
        return (fId <= this.cacheLimit) ? true : false;
    }

    @Override
    public void evictContent(int fId) {

    }
}
