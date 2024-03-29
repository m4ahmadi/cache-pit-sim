package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Entry;

import java.util.HashMap;


public class RenewalLRUCache extends BasicCache {

    HashMap<Integer, Entry> cacheHashMap;
    Entry start, end;
    private final int cacheLimit;
    private double CTime;


    public RenewalLRUCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, pit, index, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        cacheHashMap = new HashMap<Integer, Entry>();
        CTime = -1;
        int z = simulationInstance.getConstant().get(Constants.KEY.Z).intValue();
       // analyticCache = new AnalyticHypoLRUCache(id, downloadRate, simulation, mpopularityDist, cacheSize, z);
        analyticCache = new AnalyticMahdiehHypoLRUCache(id, downloadRate, simulation, mpopularityDist, cacheSize, z);
        //analytic value for cache with hyper input

    }

    public boolean getEntry(int key) {
        if (cacheHashMap.containsKey(key)) // Key Already Exist, just update the
        {
            Entry entry = cacheHashMap.get(key);
            removeNode(entry);
            addAtTop(entry);
            return cacheHashMap.get(key).value;
        }
        return false;
    }

    public boolean contains(int key) {
        return cacheHashMap.containsKey(key);
    }

    // for putting a null node
    private void putNull(int key) {
        if (!cacheHashMap.containsKey(key)) {
            Entry newnode = new Entry();
            newnode.left = null;
            newnode.right = null;
            newnode.value = false;
            newnode.key = key;
            if (cacheHashMap.size() > cacheLimit) // We have reached maxium size so need to make room for new element.
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

    private void putEntry(int key) {
        if (cacheHashMap.containsKey(key)) // Key Already Exist, just update the value and move it to top
        {
            Entry entry = cacheHashMap.get(key);
            if(cacheHashMap.get(key).value) {
                //entry.value = value;
                removeNode(entry);
                addAtTop(entry);
                assert false; //should not happen
            }else{
                entry.value = true;
            }
        }  else {
            Entry newnode = new Entry();
            newnode.left = null;
            newnode.right = null;
            newnode.value = true;
            newnode.key = key;
            if (cacheHashMap.size() > cacheLimit) // We have reached maxium size so need to make room for new element.
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
        if (start != null) start.left = node;
        start = node;
        if (end == null) end = start;
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
        boolean hit = getEntry(fId);
        if (!hit) {
            putNull(fId);
        }
        return hit;
    }

    @Override
    public void evictContent(int fId) {

    }
}

