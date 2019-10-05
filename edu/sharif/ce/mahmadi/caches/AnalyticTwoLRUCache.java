package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

//Two LRU with draft method but poisson
public class AnalyticTwoLRUCache extends AnalyticCache {

    private AnalyticRenewalLRUCache virtualCache;
    private final int cacheLimit;
    private double CTime;

    public AnalyticTwoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize) {
        super(id, downloadRate, simulation, mpopularityDist);
        virtualCache = new AnalyticRenewalLRUCache(id, 0, simulation, mpopularityDist, mcacheSize);
        this.cacheLimit = mcacheSize;
        CTime = -1;
    }

    private double expectedR(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        return (theoreticalDownTime + 0.5 * theoreticalDownTime * rate) / (theoreticalDownTime * rate + 1);
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double exp = -1;

        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);

        double hitPercent = (exp - 1 - rate * expectedR(fId)) / (exp - 1);
        double ZDDhit = computeAnalyticZDDHitProbability(fId);

        return (hitPercent > 0 ? hitPercent : 0) * ZDDhit;
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double prob = (1 - computeAnalyticHitProbability(fId) - computeAnalyticPitHitProbability(fId));
        return (prob > 0) ? prob * rate : 0;
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double exp = -1;
        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);
        double ZDDhit = computeAnalyticZDDHitProbability(fId);
        double pitPercent = ((rate * expectedR(fId)) / (exp - 1));
        double pit1 = pitPercent >= 1 ? ZDDhit : ZDDhit * pitPercent;
        //PIT hit second part
        if (Double.isNaN(Math.exp(-1 * rate * expectedR(fId)))) exp = Double.MIN_VALUE;
        else exp = Math.exp(-1 * rate * expectedR(fId));
        double pit2 = (1 - ZDDhit) * (1 - ZDDhit) * (1 - exp);
        return (pit1) + pit2;
    }

    private double computeAnalyticZDDHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double exp = -1;
        double q = virtualCache.computeAnalyticHitProbability(fId);
        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);
        double fx = 1 - 1 / exp;
        double ph = fx * q / (1.0 - fx * (1 - q));
        return ph;
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        return 0.0;
    }

    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 1000;
        int nrOfTry = 0;
        double epsilon = 0.000000001;
        this.CTime = 0.0001;
        double C = 0;
        nrOfTry = 0;
        do {
            nrOfTry++;
            C = cachedim();
            //   printf("%lf %lf\n",C,intpar->TC);
            //exit(0);
            CTime = CTime * 2.0;
        } while ((C < cacheLimit) && (nrOfTry < maxTry));
        if (nrOfTry == 1) {
            System.out.println("error inital TC too large");
            assert false;
        }
        if (nrOfTry == maxTry) {
            System.out.println("error initial TC  too small");
            assert false;
        }
        double TC2 = CTime / 2.0;
        double TC1 = CTime / 4.0;
        do {
            CTime = (TC1 + TC2) / 2.0;
            C = cachedim();
            //printf("b %lf %lf\n",C,intpar->TC);
            if (C < cacheLimit) TC1 = CTime;
            else TC2 = CTime;
            // printf("%lf %lf %lf  %lf \n",C,TC, TC1, TC2);
        } while (Math.abs(C - cacheLimit) / C > 0.001);
        // printf("c\n");
        System.out.println(CTime);
        return CTime;
    }

    private double cachedim() {
        double result = 0;
        for (int f = 1; f <= popularityDist.getCatalogSize(); f++) {
            double q = virtualCache.computeAnalyticHitProbability(f);
            double rate = popularityDist.getArrivalRate(f);
            double exp = -1;
            if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rate * CTime);

            double fx = 1 - 1 / exp;
            double pin = fx * q / (1.0 - fx * (1 - q));
            result += pin;
            //System.out.println(f + "_" + rate * T  + "-" + result);
        }
        return result;
    }
}
