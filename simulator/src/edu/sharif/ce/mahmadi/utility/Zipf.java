package edu.sharif.ce.mahmadi.utility;

import java.text.DecimalFormat;

public class Zipf implements Dist {

  private double alpha; // alpha parameter
  private int size; // number of contents
  private double globalSum; //sum of all popularities
  private double[] lambda; // request rate at each cp for each file
  private double[] cdf;
  private double arrivalRate; //arrival rate of requests
  boolean index;

  public Zipf(double malpha, int msize, boolean mindex) {
    super();
    this.alpha = malpha;
    this.size = msize;
    index = mindex;
    if(index) {
      lambda = new double[size];
      cdf = new double[size + 1];
    }
    arrivalRate = 0;
    setPopularity();
  }

  public Zipf(double malpha, int msize, double marrivalRate, boolean mindex) {
    super();
    this.alpha = malpha;
    this.size = msize;
    index =mindex;
    if(index) {
      lambda = new double[size];
      cdf = new double[size + 1];
    }
    arrivalRate =marrivalRate;
    setPopularity();
  }

  private void setPopularity() {
    double sum = 0;
    for (int k = 1; k <= size; k++) {
      sum = sum + 1.0 / Math.pow(k, alpha);
    }
    globalSum = sum;
    if(index) {
      for (int k = 1; k <= size; k++) {
        lambda[k - 1] = ((1.0 / Math.pow(k, alpha)) / sum);
      }
      updateCDF();
    }
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
    //return this.lambda[Id - 1];
    return ((1.0 / Math.pow(Id, alpha)) / globalSum);
  }

  @Override
  public int getCatalogSize() {
    return size;
  }

  @Override
  public double getArrivalRate(int fId) {
    return arrivalRate*getPDF(fId);
  }

  @Override
  public double getArrivalRate() {
    return arrivalRate;
  }

  @Override
  public double[] getPDF() {
    return this.lambda;
  }

  public void shuffle() {
    // randomly permute the rank of the files
    double temp;
    int place;
    for (int f = 0; f < size; f++) {
      // completely independent ranks of files across nodes
      place = RandomGen.intervalRandomInt(0, size - 1);
      // make the swap
      temp = lambda[f];
      lambda[f] = lambda[place];
      lambda[place] = temp;
    }
    updateCDF();
  }

  public void shuffle(int param) {
    // permute the rank of the files
    double temp;
    int place;
    if (param == 1)
      return;
    else {
      for (int f = 0; f < size / 2; f++) {
        // completely independent ranks of files across nodes
        place = size - f - param;
        // make the swap
        temp = lambda[f];
        lambda[f] = lambda[place];
        lambda[place] = temp;
      }
      updateCDF();
    }
  }

  public String toString() {
    DecimalFormat df = new DecimalFormat("#.##");
    StringBuilder tmp = new StringBuilder();
    for (int i = 0; i < size; i++) {
      tmp.append(df.format(lambda[i]) + " ");
    }
    tmp.append("\\\\\n");
    return tmp.toString();
  }

}
