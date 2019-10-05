package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DehghanTwoLRUCache extends BasicCache {

    DehghanLRUCache physicalCache;
    DehghanLRUCache virtualCache;
    HashMap<Integer, Integer> storeList;
    private int cacheLimit;
    private double CTime;


    public DehghanTwoLRUCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, pit, index, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        physicalCache = new DehghanLRUCache(id, false, index, downloadRate, simulation, mpopularityDist, cacheSize);
        virtualCache = new DehghanLRUCache(id, false, index, 0.0, simulation, mpopularityDist, cacheSize);
        CTime = -1;
        storeList = new HashMap<Integer, Integer>();
        int z = simulationInstance.getConstant().get(Constants.KEY.Z).intValue();
        analyticCache = new AnalyticDehghanHypoTwoLRUCache(id, downloadRate, simulation, mpopularityDist, cacheSize, z);
    }


    @Override
    protected void writeMessageInTheCache(int fId) {
        if (storeList.containsKey(fId)) {
            physicalCache.writeMessageInTheCache(fId);
            storeList.remove(new Integer(fId));
        }
        /*
        if (virtualCache.contains(fId)) {
            physicalCache.writeMessageInTheCache(fId);
            //storeList.remove(new Integer(fId));
        }
        if(!virtualCache.contains(fId) && theoreticalDownTime==0)
            virtualCache.writeMessageInTheCache(fId);*/
    }

    @Override
    protected boolean isInTheCache(int fId) {
        boolean vhit = virtualCache.isInTheCache(fId);
        if(!vhit) virtualCache.writeMessageInTheCache(fId);
        if (vhit) {
            if (!storeList.containsKey(fId)) storeList.put(fId,0);
        }
        //boolean physicalHit = physicalCache.contains(fId);
        return physicalCache.getEntry(fId);


        /*boolean vhit = virtualCache.isInTheCache(fId);
        if(!vhit && theoreticalDownTime!=0) virtualCache.writeMessageInTheCache(fId);
        //boolean physicalHit = physicalCache.contains(fId);
        return physicalCache.getEntry(fId);*/
    }

    @Override
    public void evictContent(int fId) {

    }
}

