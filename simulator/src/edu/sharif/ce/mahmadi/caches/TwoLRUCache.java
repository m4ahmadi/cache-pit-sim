package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Entry;

import java.util.ArrayList;
import java.util.List;


public class TwoLRUCache extends BasicCache {

    RenewalLRUCache physicalCache;
    RenewalLRUCache virtualCache;
    List<Integer> storeList;
    Entry start, end;
    private final int cacheLimit;
    private double CTime;


    public TwoLRUCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, pit, index, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        physicalCache = new RenewalLRUCache(id, false, index, downloadRate, simulation, mpopularityDist, cacheSize);
        virtualCache = new RenewalLRUCache(id, false, index, 0, simulation, mpopularityDist, cacheSize);
        CTime = -1;
        storeList = new ArrayList<Integer>();
        int z = simulationInstance.getConstant().get(Constants.KEY.Z).intValue();
        analyticCache = new AnalyticMahdiehHypoTwoLRUCache(id, downloadRate, simulation, mpopularityDist, cacheSize, z);
    }


    @Override
    protected void writeMessageInTheCache(int fId) {
        if (storeList.contains(fId)) {
            physicalCache.writeMessageInTheCache(fId);
            storeList.remove(new Integer(fId));
        }
    }

    @Override
    protected boolean isInTheCache(int fId) {
        boolean vhit = virtualCache.isInTheCache(fId);
        if(!vhit) virtualCache.writeMessageInTheCache(fId);
        boolean physicalHit = physicalCache.contains(fId);

        if (vhit) {
            if (physicalCache.isInTheCache(fId)) return true;
            else if (!physicalCache.isInTheCache(fId)) {
                if (!storeList.contains(fId)) storeList.add(fId);
                return false;
            }
        } else {
            if (physicalHit) return physicalCache.isInTheCache(fId);
            else return false;
        }
        return false;
    }

    @Override
    public void evictContent(int fId) {

    }
}

