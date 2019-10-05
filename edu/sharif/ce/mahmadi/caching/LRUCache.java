package edu.sharif.ce.mahmadi.caching;

import edu.sharif.ce.mahmadi.simulation.CacheNetworkSimulation;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Entry;
import edu.sharif.ce.mahmadi.utility.RandomGen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LRUCache extends Cache {

    HashMap<Integer, Entry> cacheHashMap;
    Entry start, end;
    private final int cacheLimit;
    //protected TreeMap<Double, Integer> cache; // files in the cache
    private int maxCacheSize;
    private double CTime;


    public LRUCache(int cacheSize, Constants.SCHEDULING scheduling, boolean pit, int id, double[] arrivalRate, double[] serviceRate, double downloadRate, Constants.DIST service_dist, Simulation simulation, double zipf, int mcatalogSize) {
        super(scheduling, pit, id, arrivalRate, serviceRate, downloadRate, service_dist, simulation, zipf, mcatalogSize);

        this.cacheLimit = cacheSize;
        this.maxCacheSize = 0;
        cacheHashMap = new HashMap<Integer, Entry>();
        theoreticalHit = new double[catalogSize];
        CTime = -1;
    }

    private double f(double T) {
        double result = 0;
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double c_res = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerServiceTime(1);
        double wqc = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerWaitingTime(1);

        double downloadTime = getTheoreticalDownTime();
        for (int f = 1; f <= catalogSize; f++) {
            double rate = getLambda(Constants.MESSAGE.REQUEST.ordinal() + 1) * popularityDist.getPDF(f);
            double z = (Math.exp(rate * T) - Math.exp(rate * (wqd + d_res))) / (Math.exp(rate * T) + 1 - Math.exp(rate * (wqd + d_res)) + ((downloadTime + wqd + d_res + wqc + c_res) * rate));
            result += z;
        }
        return result - cacheLimit;
    }

    private double f_prime(double T) {
        double result = 0;
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double c_res = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerServiceTime(1);
        double wqc = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerWaitingTime(1);

        double downloadTime = getTheoreticalDownTime();
        for (int f = 1; f <= catalogSize; f++) {
            double rate = getLambda(Constants.MESSAGE.REQUEST.ordinal() + 1) * popularityDist.getPDF(f);
            double z = (Math.exp(rate * T) + 1 - Math.exp(rate * (wqd + d_res)) + ((downloadTime + wqd + d_res + wqc + c_res) * rate));
            result += ((rate * Math.exp(rate * T)) * (1 + rate * (downloadTime + wqd + d_res + wqc + c_res))) / (z * z);
        }
        return result;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 1000;
        int nrOfTry = 0;
        double epsilon = 0.000000001;
        double x0 = 0;

        double x1 = 1;//300;
        while (nrOfTry < maxTry) {
            nrOfTry++;
            while (Math.abs(x0 - x1) > epsilon && x1 != Double.NaN) {
                x0 = x1;
                x1 = x0 - (f(x0) / f_prime(x0));
            }
            if (x1 > 0 && x1 != Double.NaN) break;
            else {
                x1 = RandomGen.uniform();
                x0 = 5;
                continue;
            }
        }
        System.out.println(nrOfTry);
        return x1;
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
        double c_res = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerServiceTime(1);
        double wqc = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerWaitingTime(1);
        double rate = getLambda(Constants.MESSAGE.REQUEST.ordinal() + 1) * popularityDist.getPDF(fId);
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double downloadTime = getTheoreticalDownTime();

//        if ((rate * CTime) > Double.MAX_EXPONENT)
//            theoreticalHit[fId - 1] = (Double.MAX_VALUE - 1) / ((Double.MAX_VALUE + ((downloadTime[fId - 1].getMean() + wqd + d_res) * rate)));
//        else
        theoreticalHit[fId - 1] = (Math.exp(rate * CTime) - Math.exp(rate * (wqd + d_res))) / (1 + Math.exp(rate * CTime) - Math.exp(rate * (wqc + c_res)) + ((downloadTime + wqd + d_res + wqc + c_res) * rate));
        return theoreticalHit[fId - 1];
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        CTime = (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqr = computeAnalyticServerWaitingTime(Constants.MESSAGE.REQUEST.ordinal() + 1);
        double r_res = computeAnalyticServerServiceTime(Constants.MESSAGE.REQUEST.ordinal() + 1);
        double c_res = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerServiceTime(1);
        double wqc = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerWaitingTime(1);

        double downloadTime = getTheoreticalDownTime();
        double rate = getLambda(Constants.MESSAGE.REQUEST.ordinal() + 1) * popularityDist.getPDF(fId);

        return theoreticalResponseTime[fId - 1] = (wqr + r_res) + (((downloadTime + d_res + wqd + wqc + c_res) + (0.5 * rate * (downloadTime + wqd + d_res + wqc + c_res) * (downloadTime + wqd + d_res + wqc + c_res))) / (1 + Math.exp(rate * CTime) - Math.exp(rate * (wqc + c_res)) + (downloadTime + wqd + d_res + d_res + wqc) * rate));

    }


    @Override
    public double computeAnalyticDownloadRate(int fId) {
        CTime = (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double c_res = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerServiceTime(1);
        double wqc = ((CacheNetworkSimulation) simulationInstance).getCache(2).computeAnalyticServerWaitingTime(1);
        double downloadTime = getTheoreticalDownTime();
        double rate = getLambda(Constants.MESSAGE.REQUEST.ordinal() + 1) * popularityDist.getPDF(fId);

        double z = (rate * (d_res + wqd + wqc + c_res + downloadTime) + 1 - Math.exp(rate * (d_res + wqd)) + (((rate * CTime) > Double.MAX_EXPONENT) ? Double.MAX_VALUE : Math.exp(rate * CTime))) / rate;
        return theoreticalDownloadRate[fId - 1] = (1.0 / z);
    }

    @Override
    public double computeAnalyticPitProbability(int fId) {
        double d_res = computeAnalyticServerServiceTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double wqd = computeAnalyticServerWaitingTime(Constants.MESSAGE.DATA.ordinal() + 1);
        double downloadTime = getTheoreticalDownTime();
        return theoreticalPitHitProb[fId - 1] = (d_res + wqd + downloadTime) * computeAnalyticDownloadRate(fId);
    }

    private boolean getEntry(int key) {
        if (cacheHashMap.containsKey(key)) // Key Already Exist, just update the
        {
            Entry entry = cacheHashMap.get(key);
            removeNode(entry);
            addAtTop(entry);
            return true;
        }
        return false;
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
    protected void writeMessageInTheCache(int fId, double time) {

       putEntry(fId);

    }

    @Override
    protected boolean isInTheCache(int fId, double time) {
        return getEntry(fId);
    }

    @Override
    public void evictContent(int i, double time) {
    }

    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }
}
