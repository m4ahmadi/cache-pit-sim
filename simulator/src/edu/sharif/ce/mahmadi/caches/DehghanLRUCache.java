package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.utility.Entry;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

import java.util.HashMap;


public class DehghanLRUCache extends BasicCache {

    HashMap<Integer, Entry> cacheHashMap;
    Entry start, end;
    private final int cacheLimit;
    private double CTime;


    public DehghanLRUCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, pit, index, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        cacheHashMap = new HashMap<Integer, Entry>();
        CTime = -1;
        int z = simulationInstance.getConstant().get(Constants.KEY.Z).intValue();
        analyticCache = new AnalyticDehghanHypoLRUCache(id, downloadRate, simulation, mpopularityDist, cacheSize, z);
    }

    public boolean getEntry(int key) {
        if (cacheHashMap.containsKey(key)) // Key Already Exist, just update the
        {
            Entry entry = cacheHashMap.get(key);
            removeNode(entry);
            addAtTop(entry);
            return true;
        }
        return false;
    }

    public boolean contains(int key){
        return cacheHashMap.containsKey(key);
    }

    private void putEntry(int key) {
        if (cacheHashMap.containsKey(key)) // Key Already Exist, just update the value and move it to top
        {
            Entry entry = cacheHashMap.get(key);
            //entry.value = value;
            removeNode(entry);
            addAtTop(entry);
        } else {
            Entry newnode = new Entry();
            newnode.left = null;
            newnode.right = null;
            //newnode.value = value;
            newnode.key = key;
            if (cacheHashMap.size() >= cacheLimit) // We have reached maxium size so need to make room for new element.
            {
                cacheHashMap.remove(end.key);
                removeNode(end);
                addAtTop(newnode);

            } else {
                addAtTop(newnode);
            }
            cacheHashMap.put(key, newnode);
        }
    }

    private void addAtTop(Entry node) {
        node.right = start;
        node.left = null;
        if (start != null)
            start.left = node;
        start = node;
        if (end == null)
            end = start;
    }

    private void removeNode(Entry node) {

        if (node.left != null) {
            node.left.right = node.right;
        } else {
            start = node.right;
        }

        if (node.right != null) {
            node.right.left = node.left;
        } else {
            end = node.left;
        }
    }

    @Override
    protected void writeMessageInTheCache(int fId) {
        putEntry(fId);
    }

    @Override
    protected boolean isInTheCache(int fId) {
         return getEntry(fId);
    }

    @Override
    public void evictContent(int fId) {

    }
}

