package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.*;
import umontreal.ssj.probdist.ErlangDist;

import java.text.DecimalFormat;

import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.MultiClassPareto;
import umontreal.ssj.probdist.ErlangDist;

import java.text.DecimalFormat;

import static java.lang.Math.*;

public class AnalyticMultiClassMahdiehONOFFTwoLRUCache extends AnalyticCache {


    private final int cacheLimit;
    private AnalyticMultiClassMahdiehONOFFLRUCache virtualCache;

    private double CTime;
    MultiClassPareto paretoDist;

    public AnalyticMultiClassMahdiehONOFFTwoLRUCache(int id, double downloadRate, Simulation simulation, Dist mpopularityDist, int mcacheSize) {
        super(id, downloadRate, simulation, mpopularityDist);
        this.cacheLimit = mcacheSize;
        paretoDist = ((MultiClassPareto) popularityDist);
        theoreticalDownTime = theoreticalDownTime/86400.0;
        CTime = -1;
        virtualCache = new AnalyticMultiClassMahdiehONOFFLRUCache(id, 0.0, simulation, mpopularityDist, mcacheSize);

        System.out.println("[" + "F=" + new DecimalFormat("0.0E0").format(popularityDist.getCatalogSize()) + "]" + "[D=" + new DecimalFormat("0.0E0").format(theoreticalDownTime) + "]" + "[c=" + new DecimalFormat("0.0E0").format(cacheLimit) + "]" + "[rate=" + new DecimalFormat("0.0E0").format(popularityDist.getArrivalRate()) + "]");
    }


    private double meanR(int fId) {
        double onRate = paretoDist.getOnArrivalRate(fId);
        double common = onRate + 1 / paretoDist.getT_ON(fId) + 1 / paretoDist.getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / paretoDist.getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);

        if (paretoDist.getT_OFF(fId) == Double.MAX_VALUE) {
            p = 0.5;
            x1 = onRate;
            x2 = onRate;
        }

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
        double onRate = paretoDist.getOnArrivalRate(fId);
        double common = onRate + 1 / paretoDist.getT_ON(fId) + 1 / paretoDist.getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / paretoDist.getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);

        if (paretoDist.getT_OFF(fId) == Double.MAX_VALUE) {
            p = 0.5;
            x1 = onRate;
            x2 = onRate;
        }

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

    @Override
    public double computeAnalyticHitProbability(int fId) {
        CTime = getCTime();
        double a = CumF(fId, CTime);
        double q_filter = virtualCache.computeAnalyticHitProbability(fId);
        double expectedMD = counting_function(fId, theoreticalDownTime);
        double p = (1 - pow((1 - q_filter), (expectedMD+1)));
        double q = qIntegral(fId) * p;
        //double q = a * p;
        double phit = (1 - a + q) != 0 ? (q / (1 - a + q)) : 1;
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

    private double qPrimeIntegral(int fId) {
        double cv = CV(fId);

        if (cv < 0.09 || theoreticalDownTime < 0.01) {
            return CumG(fId, CTime + meanR(fId)) - CumG(fId, meanR(fId));
        }

        PhaseFit fit = new PhaseFit();
        fit.fitTwoMomentsErlang(meanR(fId), cv);
//        fit.fitThreeMoment(meanR(fId), m2(fId), m3(fId));
//        fit.fitFirstMoment(meanR(fId));

        /*else if (0.09 <= cv && cv < 0.10) {//12
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
        double exp = -1;*/

        double fx = 0;
        double sum = 0;
        double sumDelat = 5;
        double width = 0.01;
        double xMin = 0.0;
        double xMax = width;

        //computing normalizing factor
        double normal = fit.cdf(theoreticalDownTime);

        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumG(fId, CTime + ave) - CumG(fId, ave);
            fx = fit.pdf(ave);
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;

    }

    private double qIntegral(int fId) {
        double cv = CV(fId);

        if (cv < 0.09 || theoreticalDownTime < 0.01) {
            return CumF(fId, CTime + meanR(fId)) - CumF(fId, meanR(fId));
        }

        PhaseFit fit = new PhaseFit();
        fit.fitTwoMomentsErlang(meanR(fId), cv);
//      fit.fitThreeMoment(meanR(fId), m2(fId), m3(fId));
//      fit.fitFirstMoment(meanR(fId));

        /*else if (0.09 <= cv && cv < 0.10) {//12
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
        double exp = -1;*/

        double fx = 0;
        double sum = 0;
        double width = 0.01;
        double sumDelat = 5;
        double xMin = 0.0;
        double xMax = width;
        //computing normalizing factor
        double normal = fit.cdf(theoreticalDownTime);
        //double normal = 1;
        while (xMax <= theoreticalDownTime) {
            double ave = (xMin + xMax) / 2;
            double value = CumF(fId, CTime + ave) - CumF(fId, ave);
            fx = fit.pdf(ave);
            sumDelat = value * width * fx / normal;
            sum += sumDelat;
            xMin = xMax;
            xMax += width;
        }
        return sum;
    }

    private double cachedim() {
        double result = 0;
        for (int f = 1; f <= popularityDist.getCatalogSize(); f++) {
            double q_filter = virtualCache.computeAnalyticHitProbability(f);
            double expectedMD = counting_function(f, theoreticalDownTime);
            double p =  (1 - pow((1 - q_filter), (expectedMD+1)));

            double qPrime = qPrimeIntegral(f) * p;
            double q = qIntegral(f) * p;

            double b = CumG(f, CTime);
            double a = CumF(f, CTime);
            //double qPrime = b * p;
            //double q = a * p;

            //double pin = (1 - a + q) != 0 ? ((qPrime) / (1 - a + q)) : 1;
            double pin = (1 - a + q) != 0 ? ((q * b + qPrime * (1 - a)) / (1 - a + q)) : 1;

            //System.out.println(pin);
            result += pin;
        }
        return result;
    }

    private double CumG(int fId, double time) {
        double onRate = paretoDist.getOnArrivalRate(fId);
        double common = onRate + 1 / paretoDist.getT_ON(fId) + 1 / paretoDist.getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / paretoDist.getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);
        double ave = (p / x1 + (1 - p) / x2);

        if (paretoDist.getT_OFF(fId) == Double.MAX_VALUE) {
            p = 1.0;
            x1 = onRate;
            x2 = 0.0;
            ave = (p / x1);
        }

        double exp1 = -1;
        if (Double.isInfinite(Math.exp(x1 * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(x1 * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(x2 * time))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(x2 * time);

        return (p != 1.0) ? 1.0 - (p / (ave * x1 * exp1)) - ((1.0 - p) / (ave * x2 * exp2)) : ((p) - ((p) / exp1));
    }

    private double CumF(int fId, double time) {
        double onRate = paretoDist.getOnArrivalRate(fId);
        double common = onRate + 1 / paretoDist.getT_ON(fId) + 1 / paretoDist.getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / paretoDist.getT_OFF(fId)));
        double x1 = 0.5 * (common + common2);
        double x2 = 0.5 * (common - common2);
        double p = (onRate - x2) / (x1 - x2);

        if (paretoDist.getT_OFF(fId) == Double.MAX_VALUE) {
            p = 1.0;
            x1 = onRate;
            x2 = 0.0;
        }

        double exp1 = -1;
        if (Double.isInfinite(Math.exp(x1 * time))) exp1 = Double.MAX_VALUE;
        else exp1 = Math.exp(x1 * time);
        double exp2 = -1;
        if (Double.isInfinite(Math.exp(x2 * time))) exp2 = Double.MAX_VALUE;
        else exp2 = Math.exp(x2 * time);

        return ((p) - ((p) / exp1)) + ((1 - p) - ((1 - p) / exp2));
    }

    double counting_function(int fId, double time) {
        double onRate = paretoDist.getOnArrivalRate(fId);
        if (paretoDist.getT_OFF(fId) == Double.MAX_VALUE) {
            return onRate * time;
        }
        double common = onRate + 1 / paretoDist.getT_ON(fId) + 1 / paretoDist.getT_OFF(fId);
        double common2 = sqrt(pow(common, 2) - (4 * onRate / paretoDist.getT_OFF(fId)));
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

    public double computeAnalyticPerClassHitProbability(int classId) {
        int getEndInClass = (paretoDist.getStartInClass(classId) + paretoDist.getNrInClass(classId)-1);
        double sum = 0;
        for (int fId=paretoDist.getStartInClass(classId) ; fId <= getEndInClass; fId++) {
            sum += paretoDist.getPDF(fId) * computeAnalyticHitProbability(fId);
        }
        return sum;
    }

    public double computeAnalyticPerClassPDF(int classId) {
        int getEndInClass = (paretoDist.getStartInClass(classId) + paretoDist.getNrInClass(classId)-1);
        double sumPDF = 0;
        for (int fId=paretoDist.getStartInClass(classId) ; fId <= getEndInClass; fId++) {
            sumPDF += paretoDist.getPDF(fId);
        }
        return sumPDF;
    }
}

