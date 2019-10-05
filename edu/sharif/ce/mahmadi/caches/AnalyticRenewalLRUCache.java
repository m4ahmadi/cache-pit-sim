package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.RandomGen;

import static java.lang.Double.NaN;

public class AnalyticRenewalLRUCache extends AnalyticCache {

    private final int cacheLimit;
    private double CTime;

    public AnalyticRenewalLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        CTime = -1;
    }

    private double f(double T) {
        double result = 0;
        double downloadTime = theoreticalDownTime;
        for (int f = 1; f <= popularityDist.getCatalogSize(); f++) {
            double rate = popularityDist.getArrivalRate(f);
            double exp = -1;
            if (Double.isInfinite(Math.exp(rate * T))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rate * T);

            double z = 1-(1/exp);
            result += z;
            //System.out.println(f + "_" + rate * T  + "-" + result);
        }
        return result - cacheLimit;
    }

    private double f_prime(double T) {
        double result = 0;
        double downloadTime = theoreticalDownTime;
        double exp, exp2;
        for (int f = 1; f <= popularityDist.getCatalogSize(); f++) {
            double rate = popularityDist.getArrivalRate(f);
            if (Double.isInfinite(Math.exp(rate * T))) {
                exp = Double.MAX_VALUE;
                result += rate * 1/exp;
            } else {
                exp = Math.exp(rate * T);
                result += rate * 1/exp;
            }
        }
        return result;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 1000;
        int nrOfTry = 0;
        double epsilon = 0.000000001;
        double x0 = 5;
        double x1 = 3;

        while (nrOfTry < maxTry) {
            nrOfTry++;
            while (Math.abs(x0 - x1) > epsilon && x1 != NaN) {
                x0 = x1;
                x1 = x0 - (f(x0) / f_prime(x0));
                System.out.println(x1);
            }
            if (x1 > 0 && x1 != NaN && !Double.isInfinite(x1)) break;
            else {
                x1 = RandomGen.uniform();
                x0 = 5;
                continue;
            }
        }
        System.out.println(nrOfTry);
        System.out.println(x1 + "-" + f(x1));

        return x1;
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();

        double rate = popularityDist.getArrivalRate(fId);
        double downloadTime = theoreticalDownTime;

        double exp = -1;
        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);
        double pit = ((downloadTime) * rate)/(exp);
        double ZDDhit = (1-(1/exp));
        if(pit > ZDDhit){
            return 0;
        }
         return ZDDhit - computeAnalyticPitHitProbability(fId);
    }

    public double computeAnalyticResponseTime(int fId) {
        CTime = getCTime();
        double downloadTime = theoreticalDownTime;
        double rate = popularityDist.getArrivalRate(fId);
        double exp = -1;
        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);
        return ((downloadTime + (0.5 * rate * downloadTime * downloadTime)) / (exp + (downloadTime) * rate));
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double prob = (1 - computeAnalyticHitProbability(fId) - computeAnalyticPitHitProbability(fId));
        return (prob > 0) ? prob * rate : 0;
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        CTime = getCTime();
        double downloadTime = theoreticalDownTime;
        double rate = popularityDist.getArrivalRate(fId);
        double exp = -1;
        if (Double.isInfinite(Math.exp(rate * CTime))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rate * CTime);
        double pit = ((downloadTime) * rate)/(exp);
        if(pit > 1)
            pit = (downloadTime * rate)/(downloadTime * rate+1);

        return pit;
    }


    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

}

