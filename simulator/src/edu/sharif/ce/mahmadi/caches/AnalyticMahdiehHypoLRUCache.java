

package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.PhaseFit;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class AnalyticMahdiehHypoLRUCache extends AnalyticCache {

    private final int cacheLimit;
    private double CTime;
    private int z;


    public AnalyticMahdiehHypoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize, int mz) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = mcacheSize;
        CTime = -1;
        z = mz;
    }

    private double sumR(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double p = ((double) z / (z + 1));
        double x1 = z * rate;
        double x2 = (1.0 / (double) z) * rate;
        double B = ((1 - p) * x1 + p * x2);
        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 2)) / B / 2;
        double exp = theoreticalDownTime + (exp(-1 * B * theoreticalDownTime) - 1) / B;
        double secondPart = z != 1 ? exp * C : 0;
        double sumR = (theoreticalDownTime + firstPart + secondPart);
        if (sumR < 0) assert sumR >= 0;
        return sumR;
    }

    private double meanR(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double p = ((double) z / (z + 1));
        double x1 = z * rate;
        double x2 = (1.0 / (double) z) * rate;
        double B = ((1 - p) * x1 + p * x2);
        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 2)) / B / 2;
        double exp = theoreticalDownTime + (exp(-1 * B * theoreticalDownTime) - 1) / B;
        double secondPart = z != 1 ? exp * C : 0;
        double expectedR = (theoreticalDownTime + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
        if (expectedR < 0) assert expectedR >= 0;
        return expectedR;
    }

    private double varianceR(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double p = ((double) z / (z + 1));
        double x1 = z * rate;
        double x2 = (1.0 / (double) z) * rate;
        double B = ((1 - p) * x1 + p * x2);
        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 3)) / B / 3;
        double exp = pow(theoreticalDownTime, 2) - (2 * theoreticalDownTime / B) - ((2 * exp(-1 * B * theoreticalDownTime) - 2) / pow(B, 2));
        double secondPart = z != 1 ? exp * C : 0;
        double expectedR = (pow(theoreticalDownTime, 2) + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
        if (expectedR < 0) assert expectedR >= 0;
        double varince = expectedR - pow(meanR(fId), 2);
        return varince;
    }

//    private double m2(int fId) {
//        double rate = popularityDist.getArrivalRate(fId);
//        double p = ((double) z / (z + 1));
//        double x1 = z * rate;
//        double x2 = (1.0 / (double) z) * rate;
//        double B = ((1 - p) * x1 + p * x2);
//        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
//        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 3)) / B / 3;
//        double exp = pow(theoreticalDownTime, 2) - (2 * theoreticalDownTime / B) - ((2 * exp(-1 * B * theoreticalDownTime) - 2) / pow(B, 2));
//        double secondPart = z != 1 ? exp * C : 0;
//        double expectedR = (pow(theoreticalDownTime, 2) + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
//        if (expectedR < 0) assert expectedR >= 0;
//        return expectedR;
//    }
//
//    private double m3(int fId) {
//        double rate = popularityDist.getArrivalRate(fId);
//        double p = ((double) z / (z + 1));
//        double x1 = z * rate;
//        double x2 = (1.0 / (double) z) * rate;
//        double B = ((1 - p) * x1 + p * x2);
//        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
//        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 4)) / B / 4;
//        double exp = pow(theoreticalDownTime, 3) - (3 * theoreticalDownTime / B) - ((3 * exp(-1 * B * theoreticalDownTime) - 3) / pow(B, 3));
//        double secondPart = z != 1 ? exp * C : 0;
//        double expectedR = (pow(theoreticalDownTime, 3) + firstPart + secondPart) / (counting_function(rate, theoreticalDownTime) + 1);
//        if (expectedR < 0) assert expectedR >= 0;
//        return expectedR;
//    }

    private double CV(int fId) {
        double c = meanR(fId) != 0 ? (varianceR(fId) / pow(meanR(fId), 2)) : 0;
        //System.out.println("fId: " + fId + ":" + c + ": " + meanR(fId) + ": " + varianceR(fId));
        return c;
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double a = CumF(rate, CTime);
        double q = qIntegral(fId);
        double phit = (1 - a + q) != 0 ? (q / (1 - a + q)) : 1;
        //double phit = (MCycle(fId))/(MCycle(fId)+1+counting_function(rate,theoreticalDownTime));
        return phit;
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double expectedD = counting_function(rate, theoreticalDownTime);
        double hit = computeAnalyticHitProbability(fId);
        //double z = (MCycle(fId) + 1 + counting_function(rate, theoreticalDownTime)) / rate;
        //return 1 / z;
        double z = ((1 - hit) * (1 / (1 + expectedD))) * rate;
        return z;
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double prob = (1 - computeAnalyticHitProbability(fId) - (computeAnalyticForwardingRate(fId) / rate));
        //double pitPercent = (counting_function(rate, theoreticalDownTime)) / (MCycle(fId) + 1 + counting_function(rate, theoreticalDownTime));
        return (prob > 0) ? prob : 0;
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double waitingTime2 = sumR(fId);
        //System.out.println(waitingTime + "\t" + waitingTime2);
        double count2 = 1 + counting_function(rate, theoreticalDownTime) + MCycle(fId);
        //System.out.println(count1 + "\t" + count2);
        return (waitingTime2 / count2);
    }

    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 100000;
        int nrOfTry = 0;
        //this.CTime = 0.000000000001;
        this.CTime = cacheLimit / popularityDist.getArrivalRate();
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
            //double q1 = CumF(rate, CTime + R(f)) - CumF(rate, R(f));
            //double qPrime1 = CumG(rate, CTime + R(f)) - CumG(rate, R(f));
            double qPrime = qPrimeIntegral(f);
            double q = qIntegral(f);
            double b = CumG(rate, CTime);
            double a = CumF(rate, CTime);
            double pin = (1 - a + q) != 0 ? ((q * b + qPrime * (1 - a)) / (1 - a + q)) : 1;
            result += pin;
            //if (f == popularityDist.getCatalogSize())
            //System.out.println(f + "-" + pin);
        }
        System.out.println("C: " + result);
        return result;
    }

    private double qPrimeIntegral(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double cv = CV(fId); //moment
        if (cv < 0.09)   return CumG(rate, CTime + meanR(fId)) - CumG(rate, meanR(fId));
        //System.out.println(fId + "\t" + cv + "\t" + meanR(fId));

        PhaseFit fit = new PhaseFit();
        fit.fitTwoMomentsErlang(meanR(fId), cv);
//        fit.fitThreeMoment(meanR(fId), m2(fId), m3(fId));
//        fit.fitFirstMoment(meanR(fId));

        double normal = fit.cdf(theoreticalDownTime);
        //System.out.println(fId + "\t" + cv + "\t" + normal);

        double fx = 0;
        double sum = 0;
        double sumDelat = 5;
        double width = 0.01;
        double xMin = 0.0;
        double xMax = width;

        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumG(rate, CTime + ave) - CumG(rate, ave);
            fx = fit.pdf(ave);
            assert  fx >= 0;
            //System.out.println("---------------" + fx);
            assert normal != 0;
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;
    }

    private double qIntegral(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double cv = CV(fId); //moment
        if (cv < 0.09)   return CumF(rate, CTime + meanR(fId)) - CumF(rate, meanR(fId));
//        if (cv == 0)   return CumF(rate, CTime + meanR(fId)) - CumF(rate, meanR(fId));

        PhaseFit fit = new PhaseFit();
        fit.fitTwoMomentsErlang(meanR(fId), cv);
//        fit.fitThreeMoment(meanR(fId), m2(fId), m3(fId));
//        fit.fitFirstMoment(meanR(fId));

        double fx = 0;
        double sum = 0;
        double width = 0.01;
        double sumDelat = 5;
        double xMin = 0.0;
        double xMax = width;

        //computing normalizing factor
        double normal = fit.cdf(theoreticalDownTime);
        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumF(rate, CTime + ave) - CumF(rate, ave);
            fx = fit.pdf(ave);
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;
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
        //System.out.println ("rate="+ lambda + "z=" + z);
        assert (firstPart + secondPart) >= 0;
        return firstPart + secondPart;
    }

    double MCycle(int fId) {
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        double rate = popularityDist.getArrivalRate(fId);
        double h = computeAnalyticHitProbability(fId);
        double expectedD = counting_function(rate, theoreticalDownTime);
        return (h != 1) ? (h * (expectedD + 1)) / (1 - h) : Double.MAX_VALUE;
        /*double phit = CumF(rate, CTime);
        double hin = theoreticalDownTime==0 ? CumF(rate, CTime) : CumG(rate, CTime);
        return phit != 1 ? (hin / (1 - phit)) : Double.MAX_VALUE;*/
    }
}

