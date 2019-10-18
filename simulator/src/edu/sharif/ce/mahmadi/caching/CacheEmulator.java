package edu.sharif.ce.mahmadi.caching;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.RandomGen;
import edu.sharif.ce.mahmadi.utility.Statistic;

public class CacheEmulator extends Cache {

    private double analyticHit;

    public CacheEmulator(double hit, Constants.SCHEDULING scheduling, boolean pit, int id, double[] arrivalRate, double[] serviceRate, double downloadTime, Constants.DIST service_dist, Simulation simulation, double zipf, int catalogSize) {
        super(scheduling, pit, id, arrivalRate, serviceRate, downloadTime, service_dist, simulation, zipf, catalogSize);
        analyticHit = hit;
    }

    @Override
    public double computeAnalyticHitProbability() {
        return analyticHit;
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        return analyticHit;
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        return (getTheoreticalDownTime());
    }

    @Override
    public double computeAnalyticExpectedPITSize() {
        return 0;
    }

    @Override
    public double computeAnalyticExpectedCacheSize() {
        return 0;
    }

    @Override
    public double computeAnalyticResponseTime() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticResponseTime(f+1));
        }
        return mean.getMean();
    }

    @Override
    public double computeAnalyticDownloadRate(int fId) {
        return analyticHit;
    }

    @Override
    public double computeAnalyticPitProbability(int fId) {
        return 0;
    }

    @Override
    public double getCTime() {
        return 0;
    }

    @Override
    protected void writeMessageInTheCache(int fId, double time) {

    }

    @Override
    protected boolean isInTheCache(int fId, double time) {
        boolean hit = RandomGen.uniform() < analyticHit;
        return hit;
    }

    @Override
    public void evictContent(int i, double time) {

    }

}
