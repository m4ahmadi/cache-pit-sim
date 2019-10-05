

package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import umontreal.ssj.probdist.ErlangDist;

import static java.lang.Math.*;

public class AnalyticMahdiehHypoTwoLRUCache extends AnalyticCache {

    private final int cacheLimit;
    private AnalyticHypoLRUCache virtualCache;
    private double CTime;
    private int z;


    public AnalyticMahdiehHypoTwoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize, int mz) {
        super(id, downloadRate, simulation, mpopularityDist);
        virtualCache = new AnalyticHypoLRUCache(id, 0.0, simulation, mpopularityDist, mcacheSize, mz);
        this.cacheLimit = mcacheSize;
        CTime = -1;
        z = mz;
    }

    private double sumR(int fId){
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

    private double CV(int fId) {
        return meanR(fId) != 0 ? (varianceR(fId) /  pow(meanR(fId), 2)) : 0;
    }

    private double R(int fId) {
        //double C  =  CV(fId);
        // System.out.println("fId: " + fId + ":" + C + ": " + expectedR(fId) + ": " + expectedRTwo(fId));
        return meanR(fId);
        //- 0.6 *expectedRTwo(fId);
    }

  /*  private double expectedAge(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double p = ((double) z / (z + 1));
        double x1 = z * rate;
        double x2 = (1.0 / (double) z) * rate;
        double expected = (p / x1) + (1 - p) / x2;
        double expectedTwo = (p / pow(x1, 2)) + ((1 - p) / pow(x2, 2));
        return expectedTwo / (2 * expected);
    }*/

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double a = CumF(rate, CTime);
        double q_filter = virtualCache.computeAnalyticHitProbability(fId);
        double expectedMD = counting_function(rate, theoreticalDownTime);
        double p = (1 - pow((1 - q_filter), (expectedMD+1)));
        double q = qIntegral(fId) * p;
        double phit = (1 - a + q) != 0 ? (q / (1 - a + q)) : 1;
        //double phit = (MCycle(fId))/(MCycle(fId)+1+counting_function(rate,theoreticalDownTime));
        return phit;
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double expectedMD = counting_function(rate, theoreticalDownTime);
        //double q_filter = virtualCache.computeAnalyticHitProbability(fId);
        //double p = (1 - pow((1 - q_filter), (expectedMD + 1)));
        double hit = computeAnalyticHitProbability(fId);
        double z = ((1 - hit) * (1 / (1 + expectedMD))) * rate;
        return z;
        //double z = (1 + expectedMD + MCycle(fId));
        //double cycle = (p * z + (1 - p) * (expectedMD + 1));
        //return rate / cycle;
    }

    @Override
    public double computeAnalyticPitHitProbability(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double pitPercent = 1 - computeAnalyticHitProbability(fId) - (computeAnalyticForwardingRate(fId) / rate);
        return (pitPercent > 0) ? pitPercent : 0;
    }

    @Override
    public double computeAnalyticResponseTime(int fId) {
        CTime = getCTime();
        double rate = popularityDist.getArrivalRate(fId);
        double waitingTime2 =  sumR(fId);
        double count2 = 1 + counting_function(rate, theoreticalDownTime) + MCycle(fId);
        return (waitingTime2/ count2);
    }


    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

    public double computeAnalyticalCharacteristicTime() {
        int maxTry = 1000;
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
            double q_filter = virtualCache.computeAnalyticHitProbability(f);
            double expectedMD = counting_function(rate, theoreticalDownTime);
            double p =  (1 - pow((1 - q_filter), (expectedMD+1)));
            //double p = q_filter;
            double qPrime = qPrimeIntegral(f) * p;
            double q = qIntegral(f) * p;
            double b = CumG(rate, CTime);
            double a = CumF(rate, CTime);
            double pin = (1 - a + q) != 0 ? ((q * b + qPrime * (1 - a)) / (1 - a + q)) : 1;
            result += pin;
            //System.out.println(f + "_" + rate * T  + "-" + result);
        }
        return result;
    }


    int fact(int number) {
        int fact = 1;
        for (int i = 1; i <= number; i++) {
            fact = fact * i;
        }
        return fact;
    }



    private double qPrimeIntegral(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double cv = CV(fId);
        int k = 2;
        if (cv < 0.09) {
            return CumG(rate, CTime + R(fId)) - CumG(rate, R(fId));
        }else if (0.09 <= cv && cv < 0.10) {//12
            k = 11;
        }else if (0.10 <= cv && cv < 0.11) {//12
            k = 10;
        }else if (0.11 <= cv && cv < 0.12) {//12
            k = 9;
        }else if (0.12 <= cv && cv < 0.13) {//12
            k = 8;
        }else if (0.13 <= cv && cv < 0.15) {//14
            k = 7;
        }else if (0.15 <= cv && cv < 0.18) {//16
            k = 6;
        }  else if (0.18 <= cv && cv < 0.23) {//20
            k = 5;
        } else if (0.23 <= cv && cv < 0.3) {//25
            k = 4;
        } else if (0.3 <= cv && cv < 0.4) {//33
            k = 3;
        } else {//50
            k = 2;
        }

        double rateR = k / R(fId);
        //double rateR = rate;
        ErlangDist test = new ErlangDist(k, rateR);
        double exp = -1;
        double fx = 0;
        double sum = 0;
        double sumDelat = 5;
        double width = 0.01;
        double xMin = 0.0;
        double xMax = width;
        //computing normalizing factor
        double normal = test.cdf(theoreticalDownTime);
        //double normal =1;
        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumG(rate, CTime + ave) - CumG(rate, ave);
            if (Double.isInfinite(Math.exp(rateR * ave))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rateR * ave);
            fx = pow(rateR, k) * pow(ave, k - 1) / (exp * fact(k - 1));
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;

    }

    private double qIntegral(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double cv = CV(fId);
        int k = 2;
        if (cv < 0.09) {
            return CumF(rate, CTime + R(fId)) - CumF(rate, R(fId));
        }else if (0.09 <= cv && cv < 0.10) {//12
            k = 11;
        }else if (0.10 <= cv && cv < 0.11) {//12
            k = 10;
        }else if (0.11 <= cv && cv < 0.12) {//12
            k = 9;
        }else if (0.12 <= cv && cv < 0.13) {//12
            k = 8;
        }else if (0.13 <= cv && cv < 0.15) {//14
            k = 7;
        }else if (0.15 <= cv && cv < 0.18) {//16
            k = 6;
        }  else if (0.18 <= cv && cv < 0.23) {//20
            k = 5;
        } else if (0.23 <= cv && cv < 0.3) {//25
            k = 4;
        } else if (0.3 <= cv && cv < 0.4) {//33
            k = 3;
        } else {//50
            k = 2;
        }
        double rateR = k / R(fId);
        ErlangDist test = new ErlangDist(k, rateR);
        double exp = -1;
        double fx = 0;
        double sum = 0;
        double width = 0.01;
        double sumDelat = 5;
        double xMin = 0.0;
        double xMax = width;
        //computing normalizing factor
        double normal = test.cdf(theoreticalDownTime);
        //double normal = 1;
        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumF(rate, CTime + ave) - CumF(rate, ave);
            if (Double.isInfinite(Math.exp(rateR * ave))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rateR * ave);
            fx = pow(rateR, k) * pow(ave, k - 1) / (exp * fact(k - 1));
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
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",p, lambda, x1, x2, firstPart, secondPart);
        assert (firstPart + secondPart) >= 0;
        return firstPart + secondPart;
    }


    double MCycle(int fId) {
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",phit, lambda, x1, x2, firstPart, secondPart);
        double rate = popularityDist.getArrivalRate(fId);
        double h = computeAnalyticHitProbability(fId);
        double expectedD = counting_function(rate, theoreticalDownTime);
        double q_filter = virtualCache.computeAnalyticHitProbability(fId);
        //double p =  ((1 - pow((1 - q_filter), (expectedD+1))));
        double p = 1;
        return (h!=1 && p!=0) ? ((h * (expectedD + 1)) / ((1 - h) * p)) : (p!=0) ? Double.MAX_VALUE : 0;
        /*double phit = CumF(rate, CTime);
        double hin = theoreticalDownTime==0 ? CumF(rate, CTime) : CumG(rate, CTime);
        return phit != 1 ? (hin / (1 - phit)) : Double.MAX_VALUE;*/
    }
}

