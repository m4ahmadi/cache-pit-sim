

package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import umontreal.ssj.probdist.ErlangDist;

import java.text.DecimalFormat;

import static java.lang.Math.*;

public class AnalyticMahdiehONOFFLRUCache extends AnalyticCache {

    private final int cacheLimit;
    private double CTime;
    private double T_ON_;
    private double T_OFF_;


    public AnalyticMahdiehONOFFLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize, double onMean, int offMean) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = mcacheSize;
        CTime = -1;
        T_ON_ = onMean;
        T_OFF_ = T_ON_ * offMean;
        System.out.println("[" + "F=" + new DecimalFormat("0.0E0").format(popularityDist.getCatalogSize()) + "]"
                + "[D=" + new DecimalFormat("0.0E0").format(theoreticalDownTime) + "]" +
                "[c=" + new DecimalFormat("0.0E0").format(cacheLimit) + "]" +
                "[rate=" + new DecimalFormat("0.0E0").format(popularityDist.getArrivalRate()) + "]"
                + "[ON=" + T_ON_ + "]" + "[" + T_OFF_ + "]" );
        //z = mz;
    }

    private double getT_ON(int fId){
        return T_ON_;
        //return T_ON_/popularityDist.getArrivalRate(fId);
    }

    private double getT_OFF(int fId){
        return T_OFF_;
        //return T_OFF_/popularityDist.getArrivalRate(fId);
    }

    private double getOnArrivalRate(int fId) {
        return popularityDist.getArrivalRate(fId) * ((getT_ON(fId) + getT_OFF(fId)) / getT_ON(fId));
        //return 9.1 * popularityDist.getArrivalRate(fId);
    }



    private double meanR(int fId) {
        double onRate = getOnArrivalRate(fId);
        double common = onRate + 1 / getT_ON(fId) + 1 / getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);

        double B = ((1 - p) * x1 + p * x2);
        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 2)) / B / 2;
        double exp = theoreticalDownTime + (exp(-1 * B * theoreticalDownTime) - 1) / B;
        double secondPart = p != 1 ? exp * C : 0;
        double expectedR = (theoreticalDownTime + firstPart + secondPart) / (counting_function(fId, theoreticalDownTime) + 1);
        if (expectedR < 0) assert expectedR >= 0;
        return expectedR;
    }

    private double varianceR(int fId) {
        double onRate = getOnArrivalRate(fId);
        double common = onRate + 1 / getT_ON(fId) + 1 / getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);
        double B = ((1 - p) * x1 + p * x2);
        double C = ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2));
        double firstPart = (x1 * x2 * pow(theoreticalDownTime, 3)) / B / 3;
        double exp = pow(theoreticalDownTime, 2) - (2 * theoreticalDownTime / B) - ((2 * exp(-1 * B * theoreticalDownTime) - 2) / pow(B, 2));
        double secondPart = p != 1 ? exp * C : 0;
        double expectedR = (pow(theoreticalDownTime, 2) + firstPart + secondPart) / (counting_function(fId, theoreticalDownTime) + 1);
        if (expectedR < 0) assert expectedR >= 0;
        double varince = expectedR - pow(meanR(fId), 2);
        return varince;
    }

    private double CV(int fId) {
        double c = meanR(fId) != 0 ? (varianceR(fId) / pow(meanR(fId), 2)) : 0;
        //System.out.println("fId: " + fId + ":" + c + ": " + meanR(fId) + ": " + varianceR(fId));
        return c;
    }

    private double R(int fId) {
        // System.out.println("fId: " + fId + ":" + C + ": " + expectedR(fId) + ": " + expectedRTwo(fId));
        return meanR(fId);
    }

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double a = CumF(fId, CTime);
        double q = qIntegral(fId);
        double phit = (1 - a + q) != 0 ? (q / (1 - a + q)) : 1;
        //double phit = (MCycle(fId))/(MCycle(fId)+1+counting_function(rate,theoreticalDownTime));
        return phit;
    }

    @Override
    public double computeAnalyticForwardingRate(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double expectedD = counting_function(fId, theoreticalDownTime);
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
        return 0.0;
    }

    @Override
    public double getCTime() {
        return (CTime == -1) ? computeAnalyticalCharacteristicTime() : CTime;
    }

    public void setCTime(double mC) {CTime = mC; }

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
            //double rate = popularityDist.getArrivalRate(f);
            double qPrime = qPrimeIntegral(f);
            double q = qIntegral(f);
            double b = CumG(f, CTime);
            double a = CumF(f, CTime);
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
            return CumG(fId, CTime + R(fId)) - CumG(fId, R(fId));
        } else if (0.09 <= cv && cv < 0.10) {//12
            k = 11;
        } else if (0.10 <= cv && cv < 0.11) {//12
            k = 10;
        } else if (0.11 <= cv && cv < 0.12) {//12
            k = 9;
        } else if (0.12 <= cv && cv < 0.13) {//12
            k = 8;
        } else if (0.13 <= cv && cv < 0.15) {//14
            k = 7;
        } else if (0.15 <= cv && cv < 0.18) {//16
            k = 6;
        } else if (0.18 <= cv && cv < 0.23) {//20
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
            double value = CumG(fId, CTime + ave) - CumG(fId, ave);
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
        double cv = CV(fId);
        int k = 2;
        if (cv < 0.09) {
            return CumF(fId, CTime + R(fId)) - CumF(fId, R(fId));
        } else if (0.09 <= cv && cv < 0.10) {//12
            k = 11;
        } else if (0.10 <= cv && cv < 0.11) {//12
            k = 10;
        } else if (0.11 <= cv && cv < 0.12) {//12
            k = 9;
        } else if (0.12 <= cv && cv < 0.13) {//12
            k = 8;
        } else if (0.13 <= cv && cv < 0.15) {//14
            k = 7;
        } else if (0.15 <= cv && cv < 0.18) {//16
            k = 6;
        } else if (0.18 <= cv && cv < 0.23) {//20
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
            double value = CumF(fId, CTime + ave) - CumF(fId, ave);
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




  /*  private double qPrimeIntegral(int fId) {
        double rate = popularityDist.getArrivalRate(fId);
        double cv = CV(fId);
        if (cv == 0 ) {
            return CumG(rate, CTime + R(fId)) - CumG(rate, R(fId));
        }
        double rateR = varianceR(fId)!=0 ? 1 / sqrt(varianceR(fId)) : 0;
        double d = rateR!=0 ? meanR(fId) - (1 / rateR) : 0;
        assert d >= 0;
        //double rateR = rate;
        //ErlangDist test = new ErlangDist(k, rateR);
        double exp = -1;
        double fx = 0;
        double sum = 0;
        double sumDelat = 5;
        double width = 0.005;
        double xMin = d;
        double xMax = d + width;
        //computing normalizing factor
        if (Double.isInfinite(Math.exp(rateR * (theoreticalDownTime - d)))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rateR * (theoreticalDownTime - d));
        //double normal = rateR/exp;
        double normal = 1;
        while (xMax <= 1) {
            double ave = (xMin + xMax) / 2;
            double value = CumG(rate, CTime + ave) - CumG(rate, ave);
            if (Double.isInfinite(Math.exp(rateR * (ave - d)))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rateR * (ave - d));
            fx = rateR / exp;
            //fx2 = test.density(ave);
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
        if (cv == 0) {
            return CumF(rate, CTime + R(fId)) - CumF(rate, R(fId));
        }
        double rateR = varianceR(fId)!=0 ? 1 / sqrt(varianceR(fId)) : 0;
        double d = rateR!=0 ? meanR(fId) - (1 / rateR) : 0;
        assert d >= 0;
        double exp = -1;
        double fx = 0;
        double sum = 0;
        double sumDelat = 5;
        double width = 0.005;
        double xMin = d;
        double xMax = d + width;
        //computing normalizing factor
        if (Double.isInfinite(Math.exp(rateR * (theoreticalDownTime - d)))) exp = Double.MAX_VALUE;
        else exp = Math.exp(rateR * (theoreticalDownTime - d));
        //double normal = rateR/exp;
        double normal = 1;
        while (xMax <= 1) {
            double ave = (xMin + xMax) / 2;
            double value = CumF(rate, CTime + ave) - CumF(rate, ave);
            if (Double.isInfinite(Math.exp(rateR * (ave - d)))) exp = Double.MAX_VALUE;
            else exp = Math.exp(rateR * (ave - d));
            fx = rateR / exp;
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;
    }*/


    private double CumG(int fId, double time) {
        double onRate = getOnArrivalRate(fId);
        double common = onRate + 1 / getT_ON(fId) + 1 / getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);
        double ave = (p/x1 + (1-p)/x2);

        double exp1 = -1;
        if (Double.isInfinite(Math.exp(x1 * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(x1 * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(x2 * time))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(x2 * time);

        return 1 - (p / (ave*x1*exp1))  - ((1-p)/ (ave*x2*exp2));
    }

    private double CumF(int fId, double time) {
        double onRate = getOnArrivalRate(fId);
        double common = onRate + 1 / getT_ON(fId) + 1 / getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);

        double exp1 = -1;
        if (Double.isInfinite(Math.exp(x1 * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(x1 * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(x2 * time))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(x2 * time);

        return ((p) - ((p) / exp1)) + ((1 - p) - ((1 - p) / exp2));
    }


    double counting_function(int fId, double time) {
        double onRate = getOnArrivalRate(fId);
        double common = onRate + 1 / getT_ON(fId) + 1 / getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);
        double B = ((1 - p) * x1 + p * x2);
        double firstPart = (x1 * x2 * time) / B;
        double secondPart = B != 0 ? (1 - exp(-1 * B * time)) * ((p * (1 - p) * pow((x1 - x2), 2)) / pow(B, 2)) : 0;
        //printf ("  %lf  %lf  %lf  %lf   %lf  %lf\n",p, lambda, x1, x2, firstPart, secondPart);
        assert (firstPart + secondPart) >= 0;
        return firstPart + secondPart;
    }


}

