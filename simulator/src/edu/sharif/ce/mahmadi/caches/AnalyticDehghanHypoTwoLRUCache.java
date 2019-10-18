package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class AnalyticDehghanHypoTwoLRUCache extends AnalyticCache {

    private AnalyticDehghanHypoLRUCache virtualCache;
    private final int cacheLimit;
    private double CTime;
    private int z;


    public AnalyticDehghanHypoTwoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize, int mz) {
        super(id, downloadRate, simulation, mpopularityDist);
        virtualCache = new AnalyticDehghanHypoLRUCache(id, 0.0, simulation, mpopularityDist, mcacheSize, mz);
        this.cacheLimit = mcacheSize;
        CTime = -1;
        z = mz;
    }


    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double q = virtualCache.computeAnalyticHitProbability(fId);
        double expectedMD = counting_function(rate, theoreticalDownTime);
        double z = (1 + expectedMD + MCycle(fId));
        return (q * MCycle(fId)) / (q * z + (1 - q) * (counting_function(rate, theoreticalDownTime) + 1));
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double expectedMD = counting_function(rate, theoreticalDownTime);
        double z = (1 + expectedMD + MCycle(fId));
        double q = virtualCache.computeAnalyticHitProbability(fId);
        double cycle = (q * z + (1 - q) * (expectedMD + 1));
        return rate/cycle;
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double q = virtualCache.computeAnalyticHitProbability(fId);
        double expectedMD = counting_function(rate, theoreticalDownTime);
        double z = (1 + expectedMD + MCycle(fId));
        return 1-computeAnalyticHitProbability(fId)-(computeAnalyticForwardingRate(fId)/rate);

    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double exp1 = -1;
        if (Double.isInfinite(Math.exp(rate * CTime)))
            exp1 = Double.MAX_VALUE;
        else
            exp1 = Math.exp(rate * CTime);

        double waitingTime = theoreticalDownTime + (0.5 * theoreticalDownTime * theoreticalDownTime * rate);
        return waitingTime / (rate * theoreticalDownTime + exp1);
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
        } while (Math.abs(C - cacheLimit) / C > 0.0000001);
        // printf("c\n");
        System.out.println(CTime);
        return CTime;
    }

    private double cachedim() {
        double result = 0;
        for (int f = 1; f <= popularityDist.getCatalogSize(); f++) {
            double rate = popularityDist.getArrivalRate(f);
            double q = virtualCache.computeAnalyticHitProbability(f);
            double z = (1 + counting_function(rate, theoreticalDownTime) + MCycle(f));
            result += (q * MCycle(f)) / (q * z + (1 - q) * (counting_function(rate, theoreticalDownTime) + 1));
        }
        return result;
    }

    private double CumG(double rate, double time) {
        double p = ((double) z / (z + 1));
        double zDouble = (double) z;
        double exp1 = -1;
        if (Double.isInfinite(Math.exp(zDouble * rate * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(zDouble * rate * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(rate * time / zDouble))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(rate * time / zDouble);

        return ((1 - p) - ((1 - p) / exp1)) + (p - (p / exp2));
    }

    private double CumF(double rate, double time) {
        double p = ((double) z / (z + 1));
        double zDouble = (double) z;
        double exp1 = -1;
        if (Double.isInfinite(Math.exp(zDouble * rate * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(zDouble * rate * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(rate * time / zDouble))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(rate * time / zDouble);

        return ((p) - ((p) / exp1)) + ((1 - p) - ((1 - p) / exp2));
    }

    double counting_function(double lambda, double time) {
        double p = ((double) z / (z + 1));
        double x1 = z * lambda;
        double x2 = (1.0 / (double) z) * lambda;
        double B = ((1 - p) * x1 + p * x2);
        double firstPart = (x1 * x2 * time) / B;
        double secondPart = z != 1 ? (1 - exp(-1 * B * time)) * ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2)) : 0;
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",p, lambda, x1, x2, firstPart, secondPart);
        assert (firstPart + secondPart) >= 0;
        return firstPart + secondPart;
    }

    double MCycle(int fId) {
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        double rate = popularityDist.getArrivalRate(fId);
        double phit = CumF(rate, CTime);
        double hin = theoreticalDownTime==0 ? CumF(rate, CTime) : CumG(rate, CTime);
        return phit != 1 ? (hin / (1 - phit)) : Double.MAX_VALUE;
    }
}
