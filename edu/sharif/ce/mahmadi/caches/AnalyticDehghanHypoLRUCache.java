package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class AnalyticDehghanHypoLRUCache extends AnalyticCache {

    private final int cacheLimit;
    private double CTime;
    private int z;

    public AnalyticDehghanHypoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize, int mz) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        CTime = -1;
        z = mz;
    }


    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double z = (1+ counting_function(rate, theoreticalDownTime) + MCycle(fId));
        return  MCycle(fId)/z;
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
        double rate = popularityDist.getArrivalRate(fId);
        double z = (1+ counting_function(rate, theoreticalDownTime) + MCycle(fId));

        return counting_function(rate, theoreticalDownTime) /z;
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
        this.CTime = 0.0000000001;
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
            double z = (1+ counting_function(rate, theoreticalDownTime) + MCycle(f));
            result += MCycle(f)/z;
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

    double counting_function (double lambda, double time)
    {
        double p =((double)z/(z+1));
        double x1 =z*lambda;
        double x2 =(1.0/(double)z) * lambda;
        double B =((1-p)*x1+ p*x2);
        //double expected = ((p)/x1+ (1-p)/x2);
        double firstPart = (x1 * x2 * time) / B;
        double secondPart = z!=1 ? (1- exp(-1*B*time)) * ((p*(1-p)* pow((x1-x2), 2))/pow(B, 2)):0;
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",p, lambda, x1, x2, firstPart, secondPart);
        return firstPart + secondPart;
    }

    double MCycle(int fId) {
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        double rate = popularityDist.getArrivalRate(fId);
        double phit = CumF(rate, CTime);
        double hin = theoreticalDownTime==0 ? CumF(rate, CTime) : CumG(rate, CTime);
        return phit != 1 ? (hin / ( 1- phit)) : Double.MAX_VALUE;
    }

}

