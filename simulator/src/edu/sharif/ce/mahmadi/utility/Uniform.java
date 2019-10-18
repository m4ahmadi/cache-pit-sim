package edu.sharif.ce.mahmadi.utility;

public class Uniform implements Dist{

    private int size; // number of contents
    private double[] lambda; // request rate at each cp for each file
    private double[] cdf;

    public Uniform(int msize) {
        super();
        this.size = msize;
        lambda = new double[size];
        cdf = new double[size + 1];
        setPopularity();
    }

    private void setPopularity() {
        double sum = 0;
        for (int k = 1; k <= size; k++) {
            sum = sum + 1.0 / size;
        }
        for (int k = 1; k <= size; k++) {
            lambda[k - 1] = ((1.0 / size) / sum);
        }
        updateCDF();
    }

    private void updateCDF() {
        double comulative = 0;
        cdf[0] = 0;
        for (int k = 1; k <= size; k++) {
            comulative += lambda[k - 1];
            cdf[k] = comulative;
        }
    }

    @Override
    public int inverseCDF(double p) {
        for (int index = 0; index < size; index++)
            if (p >= cdf[index] && p < cdf[index + 1])
                return index + 1;
        return -1;
    }

    @Override
    public double getPDF(int Id) {
        return this.lambda[Id - 1];
    }

    @Override
    public int getCatalogSize() {
        return size;
    }

    @Override
    public double getArrivalRate(int Id) {
        return 0;
    }

    @Override
    public double getArrivalRate() {
        return 0;
    }

    @Override
    public double[] getPDF() {
        return this.lambda;
    }
}
