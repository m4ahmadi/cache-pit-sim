package edu.sharif.ce.mahmadi.utility;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PrimitiveStatistic {

    private double startTime; //for statistics such as meeting times
    private double stateChangeTime; //for statistics such as length
    private float[] values;
    private int nrOfValues;
    private double shiftedSum;
    private double realSum;
    private double squaredSum;
    private double maxValue;
    private double minValue;
    private double K; //constant to compute the variance in one round
    private Map<Integer, Float> valueMap;
    private boolean sorted;


    public PrimitiveStatistic() {
        //values = new float[Double.];
        shiftedSum = 0;
        sorted=false;
        realSum = 0;
        K= 5;
        squaredSum = 0;
        nrOfValues = 0;
        maxValue = Double.MIN_VALUE;
        minValue = Double.MAX_VALUE;
        startTime = -1;
        stateChangeTime = 0;
        valueMap = new TreeMap<Integer, Float>();
    }

    /**
     * Count the number of elements higher or equal than the given value. The list is sorted in
     * ascending order.
     *
     * @param sortedList
     * @param value
     * @return
     */
    private static <T extends Comparable<T>> int nrElementsGreaterEqual(List<T> sortedList, T value) {
        int count = 0;
        for (T elem : sortedList) {
            if (elem.compareTo(value) == -1) count++;
            else break;
        }

        return sortedList.size() - count;
    }

    /**
     * Count the number of elements smaller or equal than the given value. The list is sorted in
     * ascending order.
     *
     * @param sortedList
     * @param value
     * @return
     */
    private static <T extends Comparable<T>> int nrElementsSmallerEqual(List<T> sortedList, T value) {
        int count = 0;
        for (T elem : sortedList) {
            if (elem.compareTo(value) <= 0) count++;
            else break;
        }
        return count;
    }

    /**
     * Used to print statistics about the distribution of requests
     *
     * @param statFilePath
     * @param statCdfFilePath
     */
    public void printStatistics(String statFilePath, String statCdfFilePath) {

       // Collections.sort(values);
        StringBuilder out = new StringBuilder();
        try (BufferedWriter fw = new BufferedWriter(new FileWriter(statFilePath)); BufferedWriter fwCdf = new BufferedWriter(new FileWriter(statCdfFilePath))) {

            for (Float i : values) {
                out.append(i + "\n");
            }
            fw.write(out.toString());
            fw.flush();
            // Print the cumulative distribution of the considered statistics
            out.setLength(0);
            for (Float i : values) {
                //double prob = nrElementsSmallerEqual(values, i) / (double) nrOfValues;
                //SimulationLogging.getLogger().severe(nrElementsGreaterEqual(values, i) + "\t" + nrElementsSmallerEqual(values, i));
                //out.append(i + "\t" + prob + "\n");
            }
            fwCdf.write(out.toString());
            fwCdf.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addValue(double value, boolean save) {
        //if(nrOfValues==0) K = value;
        shiftedSum += value - K;
        realSum += value;
        squaredSum += ((value-K) * (value-K));
        nrOfValues++;
        if (maxValue < value) maxValue = value;
        if (minValue > value) minValue = value;

        if (save){
            if(values ==null) {
                values = new float[10 ^ 8];
            }
            values[nrOfValues-1]=(float)value;
            //values.add((float) (value));

        }
    }

    public double getMean() {
        return nrOfValues == 0 ? 0 : realSum / nrOfValues;
    }

    public double getSTD(){
        double variance = nrOfValues < 2 ? 0 : (squaredSum - ((shiftedSum * shiftedSum) / nrOfValues)) / (nrOfValues - 1);
        return Math.sqrt(variance);
    }
    public double getError() {

        double mean = getMean();
        double temp = 0;
        for (double num : values)
            temp += (mean - num) * (mean - num);
        return nrOfValues == 0 ? 0 : ((double) Math.sqrt(temp / nrOfValues) / 2);
    }

    public double getConfidenceInterval() {
        // value for 95% confidence interval, source:
        // https://en.wikipedia.org/wiki/Confidence_interval#Basic_Steps
        double confidenceLevel = 1.96;
        double temp = nrOfValues == 0 ? 0 : confidenceLevel * (getSTD() / Math.sqrt(nrOfValues));
        return temp;
    }

    public double getNormalPecentile(int percentile){
        // double[] zValues= {-0.67, 0, 0.67}; //for 25,50,75
        //return getMean()+zValues[percentile/25-1]*getSTD();

        if (nrOfValues ==0 || values==null) {
            //System.out.println("Here");
            return 0;
        } else if (nrOfValues == 1) {
            return ((double) percentile * values[0]) / 100;
        }
        if(!sorted){
           // List<Float> tmp = new ArrayList<Float>().add(0,values);
            //Collections.sort(tmp);
            sorted = true;
        }
        int index = nrOfValues * percentile + 50;
        if (index % 100 == 0) {
            // System.out.println("Percentile: " + percentile + "Size:" +
            // values.size() + " Index:" + index / 100);
            return values[index / 100 - 1];
        } else {
            int indexA = (index / 100);
            int indexB = (index / 100) + 1;
            double coef = ((double) (index % 100)) / 100;
            // System.out.println("Percentile: " + percentile + "Size:" +
            // values.size() + " Index1:" + indexA + " Index2:"
            // + indexB + " coef" + coef);
            return (coef * values[indexB - 1] + (1 - coef) * values[indexA - 1]);
        }

    }

    public float[] getValues() {
        return this.values;
    }

    public void addReplication(Statistic st) {
        shiftedSum += st.getShiftedSum();
        realSum += st.getRealSum();
        squaredSum += st.getSquaredSum();
        nrOfValues += st.getSize();
        if (maxValue < st.getMax()) maxValue = st.getMax();
        if (minValue > st.getMin()) minValue = st.getMin();
       // for(int 0; i<)
       // values.addAll(st.getValues());
    }

    public double getPercentile(int percentile) {
     //   Collections.sort(values);
        if (nrOfValues == 0 || values==null) {
            //System.out.println("Here");
            return 0;
        } else if (nrOfValues == 1) {
            return ((double) percentile * values[0]) / 100;
        }
        int index = nrOfValues * percentile + 50;
        if (index % 100 == 0) {
            // System.out.println("Percentile: " + percentile + "Size:" +
            // values.size() + " Index:" + index / 100);
            return values[index / 100 - 1];
        } else {
            int indexA = (index / 100);
            int indexB = (index / 100) + 1;
            double coef = ((double) (index % 100)) / 100;
            // System.out.println("Percentile: " + percentile + "Size:" +
            // values.size() + " Index1:" + indexA + " Index2:"
            // + indexB + " coef" + coef);
            return (coef * values[indexB - 1] + (1 - coef) * values[indexA - 1]);
        }
    }

    public double getMin() {
        return minValue;
    }

    public double getMax() {
        return maxValue;
    }

    public double realSum() {
        return realSum;
    }

    public double getShiftedSum() {
        return shiftedSum;
    }

    public int getSize() {
        return nrOfValues;
    }

    public void addEndValue(double time) {
        if (startTime != -1) addValue((time - startTime), false);
        startTime = time;
    }

    public void addSizeValue(double endTime, double value) {
        //SimulationLogging.getLogger().severe("endTime: " + endTime + " : " + getSize());
        assert endTime >= stateChangeTime;
        addValue((endTime - stateChangeTime) * value, false);
        stateChangeTime = endTime;
    }

    /**
     * Add value when we want to caclculate the integral and then get its distribution
     *
     * @param endTime
     * @param value
     * @param save
     */
    public void addSizeValue(double endTime, int value, boolean save) {
        assert endTime >= stateChangeTime;
        if(save) {
            if (valueMap.containsKey(value))
                valueMap.put(value, valueMap.get(value).floatValue() + (float) (endTime - stateChangeTime));
            if (!valueMap.containsKey(value)) valueMap.put((int) value, (float) (endTime - stateChangeTime));
        }
        addValue((endTime - stateChangeTime) * value, false);
        stateChangeTime = endTime;
    }

    /**
     * Probability density function of values, chnages the valuemap structure
     *
     * @param totalTime total time of simulation
     * @return
     */
    public Map<Integer, Float> getPdf(double totalTime) {
        for (Map.Entry<Integer, Float> entry : valueMap.entrySet()) {
            entry.setValue((float) (entry.getValue() / totalTime));
        }
        return valueMap;
    }

    public void printPdf(String statFilePath, double totalTime) {
        StringBuilder out = new StringBuilder();
        try (BufferedWriter fw = new BufferedWriter(new FileWriter(statFilePath))) {
            for (Map.Entry<Integer, Float> entry : getPdf(totalTime).entrySet()) {
                out.append(entry.getKey() + "\t" + entry.getValue() + "\n");
                //SimulationLogging.getLogger().severe(entry.getKey() + "\t" + entry.getValue());
            }
            fw.write(out.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is used when we want to calculate the time between some events
     *
     * @param totalTime of simulation
     * @return
     */
    public double getMean(double totalTime) {
        //if (startTime != -1 && getSize() == 0) return totalTime;
        if (totalTime != startTime && startTime != -1) addValue(totalTime - startTime, false);
        return getMean();
    }

    public double getSquaredSum() {
        return squaredSum;
    }

    public double getRealSum() {
        return realSum;
    }
}
