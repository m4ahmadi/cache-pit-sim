package edu.sharif.ce.mahmadi.utility;

import umontreal.ssj.probdist.ErlangDist;

/*
    A library for moment matching.
    You can choose how many number rof moments you want to match
 */
public final class PhaseFit {


    private int fitType = -1; //1:exp, 2:erlang, 3:hyperErlang, 4:phase
    private int k = 0;
    private double lambda, p;

    public PhaseFit() {
        k = 0;
        lambda = 0.0;
        p = 0.0;
    }

    public void fitFirstMoment(double m1) {
        fitType = 1;
        k = 1;
        lambda = 1.0 / m1;
    }

    public void fitTwoMoments(double m1, double cv2) {
        k = (int) Math.ceil(1 / cv2);
        p = (1.0 / (1.0 + cv2)) * (k * cv2 - Math.pow((k * (1 + cv2) - Math.pow(k, 2) * cv2), 0.5));
        lambda = (k - p) / m1;
        fitType = 3;
        //func = result;
        //System.out.println(p);
        //System.out.println(lambda);
        //System.out.println(k);
        //System.out.println(result.CV());
    }

    public void fitTwoMomentsErlang(double m1, double cv2) {
        k = (int) Math.round(1 / cv2);
        lambda = (k) / m1;
        fitType = 2;

        //System.out.println(k);
        //System.out.println(lambda);
        //System.out.println(result.CV());
    }

//    public void fitThreeMoment(double m1, double m2, double m3) {
//        MomentsACPHFit fitter = new MomentsACPHFit(m1, m2, m3);
//        func = fitter.fit();
//        fitType = 4;
//    }

//    public ContPhaseVar getFunc() {
//        return func;
//    }

    public double pdf(double x) {
        if (fitType == 1) {
            return erlang(k, lambda, x);
        } else if (fitType == 2) {
            return erlang(k, lambda, x);
        } else if (fitType == 3) {
            return hyperErlang(x);
        } else if (fitType == 4) {
            //double delta = func.cdf(x + 0.01) - func.cdf(x - 0.01);
            //return (delta > 0) ? (delta / 0.01) : 0.0;
        } else {
            assert false;
        }
        return -1;
    }

    public double cdf(double x) {
        if (fitType == 1) {
            ErlangDist test = new ErlangDist(k, lambda);
            return test.cdf(x);
        } else if (fitType == 2) {
            ErlangDist test = new ErlangDist(k, lambda);
            return test.cdf(x);
        } else if (fitType == 3) {
            ErlangDist test2 = new ErlangDist(k, lambda);
            ErlangDist test1 = new ErlangDist(k - 1, lambda);
            return p * test1.cdf(x) + (1 - p) * test2.cdf(x);
        } else if (fitType == 4) {
            //return func.cdf(x);
        } else {
            assert false;
        }
        return -1;
    }

    private double erlang(int kk, double rate, double x) {
        ErlangDist test = new ErlangDist(kk, rate);
        return test.density(x);
    }

    private double hyperErlang(double x) {
        double fx = (1 - p) * erlang(k, lambda, x) + p * erlang(k - 1, lambda, x);
        return fx;
    }

    public static long fact(int number) {
        long fact = 1;
        for (int i = 1; i <= number; i++) {
            fact = fact * i;
        }
        assert fact != 0;
        return fact;
    }

    public int getK() {
        return k;
    }

}
