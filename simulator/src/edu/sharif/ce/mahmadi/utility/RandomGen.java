package edu.sharif.ce.mahmadi.utility;

import java.util.Random;

public final class RandomGen {

  private static Random random; // pseudo-random number generator
  private static long seed; // pseudo-random number generator seed

  // static initializer
  static {
    // this is how the seed was set in Java 1.4
    seed = System.currentTimeMillis();
    random = new Random(seed);
  }

  public static void resetSeed(){
    seed = System.currentTimeMillis();
    random.setSeed(seed);
  }

  /**
   * Return real number uniformly in [0, 1).
   */
  public static double uniform() {
    return random.nextDouble();
  }


  public static double intervalRandomDouble(double min, double max) {
    return min + (uniform() * (max - min));
  }

  public static long intervalRandomLong(long min, long max) {
    return min + (long) (uniform() * (max - min));
  }

  public static int intervalRandomInt(int min, int max) {
    return min + random.nextInt(max - min + 1);
  }

  /**
   * Return a real number from an exponential distribution with rate lambda.
   */
  public static double exp(double lambda) {
    return -Math.log(1 - uniform()) / lambda;
  }


  public static double pareto(double scale, double beta) {
    return scale * Math.exp(exp(beta));
  }

  public static double hypoExp(int z, double lambda) {
    double p = ((double)z)/(z+1);
    if(uniform() < p)
      return exp(z * lambda);
    else
      return exp(lambda/z);
  }

}
