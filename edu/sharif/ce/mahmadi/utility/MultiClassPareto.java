package edu.sharif.ce.mahmadi.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * class which represents the analytic paraters of Trace 4
 */
public class MultiClassPareto implements Dist {

    private final double beta = 2.5;
    private final int nrClasses = 6;
    //private final int totalNrVideos = 263689;
    private final int totalNrVideos = 1757925;
    private int totalNrRequests = 0;


    //private final int[] nrVideos = {8807, 10706, 7146, 11813,  225217 };
    //private final int[] nrVideos = {8807, 10706, 7146, 11813, 225217,  1494236 };
    private final int[] nrVideos = {1441, 1592, 967, 1366, 33667,  1718892 };


    private int[] fIdComulative = null;

    //private final double[] l = {1.06 * 86400, 3.41 * 86400, 6.45 * 86400, 10.65 * 86400};
    private final double[] l = {1.06, 3.41, 6.45, 10.65, Double.MAX_VALUE, Double.MAX_VALUE};

    private double[] EVm = {74.0, 48.0, 60.3, 37.8, 28.1, 1.6};
    private double[] Vmin = null;
    private double[][] V; // request rate


    public MultiClassPareto() {
        super();
        V = new double[nrClasses][];
        Vmin = new double[nrClasses];
        fIdComulative = new int[nrClasses + 1];
        fIdComulative[0] = 0;
        int sum = 0;
        for (int i = 0; i < nrClasses; i++) {
            Vmin[i] = ((beta - 1) / beta) * EVm[i];
            V[i] = new double[nrVideos[i]];
            sum += nrVideos[i];
            fIdComulative[i + 1] = sum;
        }
        //setPopularity();
        setPopularityFromFile();
    }


    private void setPopularityFromFile() {
        File vmFile = new File("trace/vm.txt");
        try {
            Scanner scanner = new Scanner(vmFile);
            for (int classId = 0; classId < nrClasses; classId++) {
                for (int fId = 0; fId < nrVideos[classId]; fId++) {
                    String[] lineSplit = scanner.nextLine().trim().split("\t");
                    int id = Integer.parseInt(lineSplit[1]);
                    assert id == classId+1;
                    V[classId][fId] = Integer.parseInt(lineSplit[2]);
                    totalNrRequests += V[id - 1][fId];
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

        private void setPopularity() {
        for (int classId = 0; classId < nrClasses; classId++) {
            for (int fId = 0; fId < nrVideos[classId]; fId++) {
                V[classId][fId] = RandomGen.pareto(Vmin[classId], beta);
                totalNrRequests += V[classId][fId];
            }
        }
    }

    private int getClassId(int fId) {
        for (int classId = 0; classId < nrClasses; classId++) {
            if (fId > fIdComulative[classId] && fId <= fIdComulative[classId + 1]) return classId + 1;
        }
        return -1;
    }

    private int getIndexInClass(int classId, int fId) {
        return fId - fIdComulative[classId - 1];
    }

    @Override
    public double getPDF(int fId) {
        int classId = getClassId(fId);
        int index = getIndexInClass(classId, fId) - 1;
        return V[classId - 1][index] / totalNrRequests;
    }

    @Override
    public int getCatalogSize() {
        return totalNrVideos;
    }

    public double getT_ON(int fId) {
        int classId = getClassId(fId);
        return l[classId - 1];
    }

    public double getT_OFF(int fId) {
        int classId = getClassId(fId);
        if (classId >= 5) {
            return Double.MAX_VALUE;
        } else {
            return 10000.0 * 9.0;
            //return l[classId-1] * 9;
        }
        //return l[classId-1] * 9;

    }

    public double getOnArrivalRate(int fId) {
        int classId = getClassId(fId);
        int index = getIndexInClass(classId, fId) - 1;
        if (classId >= 5) {
            return V[classId - 1][index] / 40.0;
        }
        return V[classId - 1][index] / getT_ON(fId);
    }

    @Override
    public double getArrivalRate(int fId) {
        int classId = getClassId(fId);
        if (classId >= 5) {
            return getOnArrivalRate(fId);
        }else {
            double ratio = getT_ON(fId) / (getT_ON(fId) + getT_OFF(fId));
            return getOnArrivalRate(fId) * ratio;
        }
    }

    @Override
    public double getArrivalRate() {
        double sum = 0;
        for (int fId = 1; fId <= totalNrVideos; fId++) {
            sum += getOnArrivalRate(fId);
        }
        return sum;
    }

    @Override
    public double[] getPDF() {
        return new double[0];
    }

    @Override
    public int inverseCDF(double p) {
        return 0;
    }

    public int getNrInClass(int classId) {
        return nrVideos[classId-1];
    }

    public int getStartInClass(int classId) {
        return fIdComulative[classId-1]+1;
    }
}
