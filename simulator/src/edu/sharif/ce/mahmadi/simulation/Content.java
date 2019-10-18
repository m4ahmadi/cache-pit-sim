/***
 * @author mahdieh
 * @time utilityBasedCaching
 */
package edu.sharif.ce.mahmadi.simulation;

import java.util.Comparator;

public class Content implements Comparable<Content> {


  private int Id;
  private int size; // size in KB
  private double lastAccessTime;
  private Utility utilityFunction;

  public Content(int mid, int msize) {
    this.Id = mid;
    size = msize;
    lastAccessTime = Double.POSITIVE_INFINITY; // it is not accessed yet
  }

  public Content(int mid) {
    this.Id = mid;
    size = 1; // unit size file
  }

  public Content(int mid, Utility mUtility) {
    this.Id = mid;
    size = 1; // unit size file
    this.utilityFunction = mUtility;
  }

  public int getId() {
    return Id;
  }

  @Override
  public int hashCode() {
    return Id;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o)
      return true;
    if (o == null)
      return false;

    final Content other = (Content) o;
    return this.Id == other.Id;
  }

  @Override
  public String toString() {
    return "[File: " + this.Id + "]";
  }

  public double getLastAccessTime() {
    return lastAccessTime;
  }

  public void setLastAccessTime(double mlastAccessTime) {
    this.lastAccessTime = mlastAccessTime;
  }

  @Override
  public int compareTo(Content otherContent) {
    double otherTime = otherContent.getLastAccessTime();

    if (this.lastAccessTime == otherTime)
      return 0;
    else if (lastAccessTime > otherTime)
      return 1;
    else
      return -1;
  }

  public double computeUtility(double param, double weight) {
    return this.utilityFunction.computeUtility(param, weight);
  }

public Utility getUtility(){
  return this.utilityFunction;
}
}
