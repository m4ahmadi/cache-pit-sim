package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.RandomGen;

import static java.lang.Double.NaN;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class AnalyticLRUvIICache extends AnalyticCache {

    private final int cacheLimit;
    private double CTime;
    private double z;

    public AnalyticLRUvIICache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int cacheSize) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = cacheSize;
        CTime = -1;
        z = 1;
    }


    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double ZDDHit= CumF(rate);
        double pit = computeAnalyticPitHitProbability(fId);
        if (pit > ZDDHit) {
            return 0;
        }
        return  (MCycle(ZDDHit))/(MCycle(ZDDHit)+counting_function(rate,theoreticalDownTime)+1);
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
        double rate = popularityDist.getArrivalRate(fId);
        double ZDDHit= CumF(rate);
        double pit = (counting_function(rate,theoreticalDownTime))/(MCycle(ZDDHit)+counting_function(rate,theoreticalDownTime)+1);
        // double pit = ((downloadTime) * rate) / (exp);
        if (pit > 1) pit = (theoreticalDownTime * rate) / (theoreticalDownTime * rate + 1);

        return pit;
    }


    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 1000;
        int nrOfTry = 0;
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
            double rate = popularityDist.getArrivalRate(f);
            result += CumG(rate);
        }
        return result;
    }

    private double CumG(double rate) {
        double p = ((double) z / (z + 1));
        double zDouble = (double) z;
        double exp1 = -1;
        if (Double.isInfinite(Math.exp(zDouble * rate * CTime))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(zDouble * rate * CTime);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(rate * CTime / zDouble))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(rate * CTime / zDouble);

        return ((1 - p) - ((1 - p) / exp1)) + (p - (p / exp2));
    }

    private double CumF(double rate) {
        double p = ((double) z / (z + 1));
        double zDouble = (double) z;
        double exp1 = -1;
        if (Double.isInfinite(Math.exp(zDouble * rate * CTime))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(zDouble * rate * CTime);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(rate * CTime / zDouble))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(rate * CTime / zDouble);

        return ((p) - ((p) / exp1)) + ((1-p) - ((1-p) / exp2));
    }

    double counting_function (double lambda, double time)
    {
        double p =((double)z/(z+1));
        double x1 =z*lambda;
        double x2 =(1.0/(double)z) * lambda;
        double B =((1-p)*x1+ p*x2);
        double firstPart = (x1*x2*time)/B;
        double secondPart = z!=1 ? (1- exp(-1*B*time)) * ((p*(1-p)* pow((x1-x2), 2))/pow(B, 2)):0;
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",p, lambda, x1, x2, firstPart, secondPart);
        return firstPart + secondPart;
    }

    double MCycle(double phit){
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        return phit!=1 ? 1/((1/phit)-1) : Double.MAX_VALUE;
    }

}

