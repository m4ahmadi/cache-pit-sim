/***
 * @author mahdieh
 * @time utilityBasedCaching
 */
package edu.sharif.ce.mahmadi.simulation;


public class Utility {

  public double weight;
  public Utility(double mwieght) {
    this.weight = mwieght;
  }
  
  /***
   * @return the default value of utility function
   */
  public double computeUtility(double param) {
    return param;
  }
  
  public double computeUtility(double param, double mweight) {
    return param;
  }
  
  public double inverseDerivative(double param){
    return 0;
  }

  public double inverseDerivative(double param, double mweight) {
    return 0;
  }
  
}
