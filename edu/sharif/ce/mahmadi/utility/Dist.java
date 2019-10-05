/*** 
 * @author mahdieh
 * @time utilityBasedCaching
 */
package edu.sharif.ce.mahmadi.utility;

import java.util.Map;

/**
 * @author mahdieh
 *
 */
public interface Dist {

    public int inverseCDF(double p);
    public double getPDF(int Id);
    public int getCatalogSize();
    public double getArrivalRate(int Id);
    public double getArrivalRate();
    double[] getPDF();

}
