package edu.sharif.ce.mahmadi.simulation;

import edu.sharif.ce.mahmadi.caches.*;
import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.server.Event;
import edu.sharif.ce.mahmadi.utility.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.pow;

public class BasicCacheNetworkSimulation extends Simulation {

    private ArrayList<BasicCache> caches; // array of caches
    private Zipf popularityDis;
    private Trace trace;
    private int traceBased;
    private double trimProb;


    public BasicCacheNetworkSimulation(Constants.REF ref) {
        super(ref);
        generateServers();
        traceBased = constants.get(Constants.KEY.TRACE).intValue();
        trimProb = constants.get(Constants.KEY.TRIM).doubleValue();


    }

    private void generateServers() {

        //Generate server for this simulation
        Constants.POLICY policy = Constants.POLICY.values()[constants.get(Constants.KEY.POLICY).intValue()];
        boolean pit = constants.get(Constants.KEY.PIT).intValue() >= 1;
        final double arrivalRate = constants.get(Constants.KEY.REQUEST_RATE).doubleValue();
        double download_rate = constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        double zipf = constants.get(Constants.KEY.ZIPF).doubleValue();
        int catalog_size = constants.get(Constants.KEY.F).intValue();
        boolean index = constants.get(Constants.KEY.PRINT_INDEX).intValue() >= 1;
        int cache_size = (int) (constants.get(Constants.KEY.C).doubleValue() * catalog_size);
        popularityDis = new Zipf(zipf, catalog_size, arrivalRate, true);
        //int cache_size = (int)  1279;

        BasicCache cache = null;
        switch (policy) {
            // case TTL:
            //cache = new TTLCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
            //   break;
            case TwoLRUDehghan:
                cache = new DehghanTwoLRUCache(1, pit, index, download_rate, this, popularityDis, cache_size);
                break;
            case LRUDehghan:
                cache = new DehghanLRUCache(1, pit, index, download_rate, this, popularityDis, cache_size);
                break;
            case TwoLRUMahdeih:
                cache = new TwoLRUCache(1, pit, index, download_rate, this, popularityDis, cache_size);
                break;
            case LRUMahdieh:
                cache = new RenewalLRUCache(1, pit, index, download_rate, this, popularityDis, cache_size);
                break;
            case LRUONOFFMahdieh:
                cache = new ONOFFLRUCache(1, pit, index, download_rate, this, popularityDis, constants.get(Constants.KEY.C).intValue());
                break;
            case TwoLRUONOFFMahdieh:
                cache = new ONOFFTwoLRUCache(1, pit, index, download_rate, this, popularityDis, constants.get(Constants.KEY.C).intValue());
                break;
            case LRUMultiClassONOFFMahdieh:
                cache = new ONOFFLRUCache(1, pit, index, download_rate, this, new MultiClassPareto(), constants.get(Constants.KEY.C).intValue());
                break;
            case TwoLRUMultiClassONOFFMahdieh:
                cache = new ONOFFTwoLRUCache(1, pit, index, download_rate, this, new MultiClassPareto(), constants.get(Constants.KEY.C).intValue());
                break;
            case LFU:
                cache = new LFUCache(1, pit, index, download_rate, this, popularityDis, cache_size);
                break;
            default:
                break;
        }

        caches = new ArrayList<>();
        caches.add(cache);
    }

    public int isTraceBased() {
        return traceBased;
    }

    public double getTraceDensity() {
        return trace.getAverageRequestDensity();
    }

    public int getTraceCatalogueSize() {
        return trace.getNrOfContents();
    }

    public static void RUN(String simulationType) {

        //downloadMultiExecution();
        if (simulationType.equals("1")) downloadMultiExecution(); //Fig. 3
        else if (simulationType.equals("2")) cacheSizeMultiExecution(); //Fig. 4
        else if (simulationType.equals("3")) rateMultiExecution(); //Fig. 5
        else if (simulationType.equals("4")) catalogMultiExecution(); //Fig. 6
            //else if (simulationType.equals("5")) lifetimeMultiExecution();
            //else if (simulationType.equals("6")) downloadLifetimeMultiExecution();
            // else if (simulationType.equals("7")) cacheSizeLifetimeMultiExecution();
        else if (simulationType.equals("7")) cacheSizeEtaLifetimeMultiExecution(); //Fig. 8
        else if (simulationType.equals("8")) rateLifetimeMultiExecution(); //Fig. 7
        else if (simulationType.equals("9")) downloadAnalyticTraceMultiExecution(); //Fig. 9
        else if (simulationType.equals("12")) cacheSizeAnalyticTraceMultiExecution(); //verify the analytic value for different cache sizes
        else if (simulationType.equals("10")) traceTrimMultiExecution(); //Fig. 10
        else if (simulationType.equals("11")) traceStatistcis(); //print trace statistics for other parts
        else singleExecution();
    }

    private static void traceStatistcis() {
        File traceFile = new File("trace/trace.txt");
        try {
            Trace trace = new Trace(traceFile, 0.0);
            while (trace.readTrace() != null) {

            }
            trace.calculateEffectiveLifeTime();
            trace.printStatistics();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void singleExecution() {
        System.out.println("HERE.......");
        DecimalFormat df = new DecimalFormat("#.00000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];

        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();
        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();

        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            outputValues = new HashMap<Constants.KEY, List<Double>>();
            // execute
            Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.REQUEST_RATE, request_rate);
            singleExecution(basePath, fileName + request_rate + "-", iterationNum, catalogSize, outputValues);
            results.append(dfSci.format(request_rate) + "\t");
            for (Constants.KEY output : output_names) {
                for (int i = 0; i < outputValues.get(output).size(); i++) {
                    results.append(df.format(outputValues.get(output).get(i)) + "\t");
                }
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");

        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        //0.0, 1.0 / 0.000005, 1.0 / 0.00001, 1.0 / 0.0001, 1.0 / 0.05,  1.0 / (0.05+0.00001)
        /*0.0, 1.0 / 0.05 , 1.0 / 0.1 , 1.0 / 0.15, 1.0 / 0.2, 1.0 / 0.25, 1.0 / 0.3*/ //Fig. 3
        double[] downloadRates = {0.0, 1.0 / 3600.0, 1.0 / 7200.0, 1.0/14400.0, 1.0/21600.0,  1.0/28800.0, 1.0/36000.0}; //fig. 9

        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();

        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "-totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.DOWNLOAD_RATE.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= downloadRates.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.DOWNLOAD_RATE, downloadRates[index - 1]);
                singleExecution(basePath, fileName + new DecimalFormat("0.0E0").format(downloadRates[index - 1]), iterationNum, catalogSize, outputValues);
                results.append(df.format((downloadRates[index - 1]) != 0 ? 1 / downloadRates[index - 1] : 0) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cacheSizeMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();


        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");

        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");
            int initialSize = 100;
            while (initialSize < catalogSize) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.C, ((double) initialSize / (double) catalogSize));
                singleExecution(basePath, fileName + dfSci.format(((double) initialSize / (double) catalogSize)) + "-", iterationNum, catalogSize, outputValues);
                results.append(dfSci.format(initialSize) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
                int power = (int) Math.log10(initialSize);
                if (initialSize == (int) pow(10, (power))) initialSize = 5 * (int) pow(10, power);
                else initialSize = (int) pow(10, (power + 1));
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rateMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double[] rate = {1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5, 1.0e6};
        //double[] rate = {1e4};

        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();

        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= rate.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.REQUEST_RATE, rate[index - 1]);
                singleExecution(basePath, fileName + rate[index - 1] + "-", iterationNum, catalogSize, outputValues);
                results.append(dfSci.format(rate[index - 1]) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void catalogMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.SIMULATION_MODE, 1);
        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();


        statsDir = new File("[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.F.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");
            int initialSize = 100000;
            while (initialSize <= pow(10, 7)) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                //Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.C, ( cacheSize / (double) initialSize));
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, (initialSize));

                singleExecution(basePath, fileName + dfSci.format(initialSize) + "-", iterationNum, initialSize, outputValues);
                results.append(dfSci.format(initialSize) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
                initialSize = 10 * initialSize;
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void rateLifetimeMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.00000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double[] density = {1e-7, 1e-6, 5e-6, 1e-5, 5e-5, 1e-4, 1e-3, 0.01, 0.04, 0.05, 0.08, 0.1, 1.0, 10, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7};

        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();

        //Parameters
        double lifeTime = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.T_ON).doubleValue();
        double onTimeHour = (lifeTime) / (3600);
        double onTimeDay = (onTimeHour) / (24);
        int onTimecatalogue = (int) (catalogSize * 10 * onTimeDay);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.T_ON, lifeTime);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= density.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                double rate = (density[index - 1] * onTimecatalogue) / (10 * 86400);
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.REQUEST_RATE, rate);
                singleExecution(basePath, fileName + density[index - 1] + "-", iterationNum, catalogSize, outputValues);

                results.append(dfSci.format(density[index - 1]) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cacheSizeEtaLifetimeMultiExecution() {

        DecimalFormat df = new DecimalFormat("#.00000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double density = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();

        //Parameters
        double lifeTime = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.T_ON).doubleValue();
        double onTimeHour = (lifeTime) / (3600);
        double onTimeDay = (onTimeHour) / (24);
        int onTimecatalogue = (int) (catalogSize * 10 * onTimeDay);
        double request_rate = (density * onTimecatalogue) / (10 * lifeTime);

        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.T_ON, lifeTime);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.REQUEST_RATE, request_rate);
        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]" + "-Dir");


        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
            int initialSize = 100;
            while (initialSize <= 500000) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.C, initialSize);
                singleExecution(basePath, fileName + dfSci.format(((double) initialSize / (double) onTimecatalogue)) + "-", iterationNum, onTimecatalogue, outputValues);
                results.append(dfSci.format(initialSize) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
                int power = (int) Math.log10(initialSize);
                if (initialSize == (int) pow(10, (power))) initialSize = 5 * (int) pow(10, power);
                else initialSize = (int) pow(10, (power + 1));
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void cacheSizeLifetimeMultiExecution() {

        DecimalFormat df = new DecimalFormat("#.00000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double density = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();

        //Parameters
        double lifeTime = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.T_ON).doubleValue();
        double onTimeHour = (lifeTime) / (3600);
        double onTimeDay = (onTimeHour) / (24);
        int onTimecatalogue = (int) (catalogSize * 10 * onTimeDay);
        double request_rate = (density * onTimecatalogue) / (10 * 86400);

        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.T_ON, lifeTime);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.REQUEST_RATE, request_rate);
        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]" + "-Dir");


        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
            int initialSize = 100;
            while (initialSize <= 500000) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.C, initialSize);
                singleExecution(basePath, fileName + dfSci.format(((double) initialSize / (double) onTimecatalogue)) + "-", iterationNum, onTimecatalogue, outputValues);
                results.append(dfSci.format(initialSize) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
                int power = (int) Math.log10(initialSize);
                if (initialSize == (int) pow(10, (power))) initialSize = 5 * (int) pow(10, power);
                else initialSize = (int) pow(10, (power + 1));
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void downloadLifetimeMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000");

        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        // 0, 1/0.1, 1/0.2, 1/0.3
        double[] downloadRates = {0.0, 1.0 / 0.05, 1.0 / 0.1, 1.0 / 0.15, 1.0 / 0.2, 1.0 / 0.25, 1.0 / 0.3};
        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();

        //Parameters
        double lifeTime = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.T_ON).doubleValue();
        double onTimeHour = (lifeTime) / (3600);
        double onTimeDay = onTimeHour / 24;
        int onTimecatalogue = (int) (catalogSize * 10 * onTimeDay);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.T_ON, lifeTime);
        Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]" + "-Dir");

        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[TON=" + new DecimalFormat("0.0E0").format(onTimeHour) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "-totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.DOWNLOAD_RATE.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= downloadRates.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.DOWNLOAD_RATE, downloadRates[index - 1]);
                singleExecution(basePath, fileName + new DecimalFormat("0.0E0").format(downloadRates[index - 1]), iterationNum, onTimecatalogue, outputValues);
                results.append(df.format((downloadRates[index - 1]) != 0 ? 1 / downloadRates[index - 1] : 0) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void lifetimeMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");
        DecimalFormat dfSci = new DecimalFormat("0.0E0");
        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        double[] lifetime = {3600.0 * 24, 3600.0 * 24 * 2, 3600.0 * 24 * 7, 3600.0 * 24 * 30};

        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();

        statsDir = new File("[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.T_ON.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= lifetime.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                //Parameters
                double onTimeHour = (lifetime[index - 1]) / (3600);
                double onTimeDay = (onTimeHour) / (24);
                int onTimecatalogue = (int) (catalogSize * 10 * onTimeDay);
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.T_ON, lifetime[index - 1]);
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.F, onTimecatalogue);
                System.out.println("[F=" + onTimecatalogue + "][TON=" + lifetime[index - 1] + "]");
                // execute
                singleExecution(basePath, fileName + lifetime[index - 1] + "-", iterationNum, onTimecatalogue, outputValues);
                results.append(dfSci.format(lifetime[index - 1]) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void downloadAnalyticTraceMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");

        // creating new file
        String basePath = new String();
        File statsDir = null;

        //double[] downloadRates = {0.0, 1.0 / 1.0, 1.0 / 60.0, 1.0 / 120.0, 1.0 / 240.0, 1.0 / 360.0, 1.0 / 480.0, 1.0 / 600.0 , 1.0 / 1200.0, 1.0 / 1800.0, 1.0 / 3600.0, 1.0 / 7200.0};
        double[] downloadRates = {0.0, 1.0 / 3600.0, 1.0 / 7200.0, 1.0/14400.0, 1.0/21600.0,  1.0/28800.0, 1.0/36000.0};


        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        int cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).intValue();

        statsDir = new File("TraceAnalytic[" + "C=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        try {
            String fileName = "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "-totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.DOWNLOAD_RATE.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= downloadRates.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.DOWNLOAD_RATE, downloadRates[index - 1]);
                singleExecution(basePath, fileName + new DecimalFormat("0.0E0").format(downloadRates[index - 1]), iterationNum, catalogSize, outputValues);
                results.append(df.format((downloadRates[index - 1]) != 0 ? 1 / downloadRates[index - 1] : 0) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void traceTrimMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");

        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        //double[] p = {0.0, 0.25, 0.5, 0.75, 0.8, 0.9, 0.97,  0.980, 0.990, 0.999, 0.9999, 0.99999 };
        double[] p = {0.0};

        double request_rate = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.REQUEST_RATE).doubleValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();
        double cacheSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.C).doubleValue();
        int z = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.Z).intValue();

        statsDir = new File("TRACE [" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[" + "download=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[Z=" + z + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;


        try {
            String fileName = "[" + "F=" + new DecimalFormat("0.0E0").format(catalogSize) + "]" + "[" + "c=" + new DecimalFormat("0.0E0").format(cacheSize) + "]" + "[" + "rate=" + new DecimalFormat("0.0E0").format(request_rate) + "]" + "[" + "download=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[Z=" + z + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "-totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME, Constants.KEY.DENSITY, Constants.KEY.TRACE_CATLOG};
            results.append(Constants.KEY.TRIM.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= p.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.TRIM, p[index - 1]);
                singleExecution(basePath, fileName + new DecimalFormat("0.0E0").format(p[index - 1]), iterationNum, catalogSize, outputValues);
                results.append(df.format((p[index - 1]) != 0 ? p[index - 1] : 0) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cacheSizeAnalyticTraceMultiExecution() {
        DecimalFormat df = new DecimalFormat("#.000000");

        // creating new file
        String basePath = new String();
        File statsDir = null;

        double[] cacheSizes = {100.0, 1000.0, 10000.0, 100000.0};
        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.POLICY).intValue()];
        int iterationNum = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.ITERATION_NUM).intValue();
        double downloadRates = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).get(Constants.KEY.F).intValue();

        statsDir = new File("TraceAnalytic[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + policy + "]" + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        try {
            String fileName = "[" + "D=" + new DecimalFormat("0.0E0").format(downloadRates) + "]" + "[" + policy + "]";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "-totalResultFile-sim.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER, Constants.KEY.RESPONSE_TIME};
            results.append(Constants.KEY.C.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= cacheSizes.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.BasicCacheNetwork).put(Constants.KEY.C, cacheSizes[index - 1]);
                singleExecution(basePath, fileName + new DecimalFormat("0.0E0").format(cacheSizes[index - 1]), iterationNum, catalogSize, outputValues);
                results.append(df.format( cacheSizes[index - 1]) + "\t");
                for (Constants.KEY output : output_names) {
                    for (int i = 0; i < outputValues.get(output).size(); i++) {
                        results.append(df.format(outputValues.get(output).get(i)) + "\t");
                    }
                }
                results.append("\n");
            }
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void printStatistics(String basePath, int time) {
        DecimalFormat df = new DecimalFormat("#.000");
        try {
            //String fileName = "[" + request_rate + "-" + new DecimalFormat("0.0E0").format(catalogSize) + "-" + new DecimalFormat("0.0E0").format(cacheSize) + "-";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + "-midResultFile-" + ".txt", true);
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.HIT, Constants.KEY.HIT_RATE, Constants.KEY.PIT_HIT, Constants.KEY.PIT_HIT_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_NUMBER};
            //results.append(Constants.KEY.DOWNLOAD_RATE.toString() + "\t");
            for (Constants.KEY output : output_names) {
                //results.append(output.toString() + "\t");
            }
            //results.append("\n");

            outputValues = new HashMap<Constants.KEY, List<Double>>();

            outputValues.put(Constants.KEY.HIT, new LinkedList<Double>());
            outputValues.get(Constants.KEY.HIT).add(getCache(0).computeAnalyticHitProbability());
            outputValues.get(Constants.KEY.HIT).add(getCache(0).getHitProbability());
            //outputValues.get(Constants.KEY.HIT).add(hitProb[id].getConfidenceInterval());

            //Hit Rate
            outputValues.put(Constants.KEY.HIT_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.HIT_RATE).add(getCache(0).computeAnalyticHitRate());
            outputValues.get(Constants.KEY.HIT_RATE).add(getCache(0).getHitRate());

            //Pit Hit Prob
            outputValues.put(Constants.KEY.PIT_HIT, new LinkedList<Double>());
            outputValues.get(Constants.KEY.PIT_HIT).add(getCache(0).computeAnalyticPitHitProbability());
            outputValues.get(Constants.KEY.PIT_HIT).add(getCache(0).getPitHitProbability());

            outputValues.put(Constants.KEY.PIT_HIT_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.PIT_HIT_RATE).add(getCache(0).computeAnalyticPitHitRate());
            outputValues.get(Constants.KEY.PIT_HIT_RATE).add(getCache(0).getPITHitRate());


            outputValues.put(Constants.KEY.FORWARDING_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_RATE).add(getCache(0).computeAnalyticForwardingRate());
            outputValues.get(Constants.KEY.FORWARDING_RATE).add(getCache(0).getForwardingRate());

            outputValues.put(Constants.KEY.FORWARDING_PROB, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_PROB).add(getCache(0).computeAnalyticForwardingProbability());
            outputValues.get(Constants.KEY.FORWARDING_PROB).add(getCache(0).getForwardingProbability());

            outputValues.put(Constants.KEY.FORWARDING_NUMBER, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(getCache(0).computeAnalyticForwardingNumber());
            outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(getCache(0).getForwardingNumber());


            results.append(time + "\t");
            for (Constants.KEY output : output_names) {
                for (int i = 0; i < outputValues.get(output).size(); i++) {
                    results.append(df.format(outputValues.get(output).get(i)) + "\t");
                }
            }
            results.append("\n");
            stats.write(results.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void singleExecution(String basePath, String fileName, int iterationNum, int catalogSize, Map<Constants.KEY, List<Double>> outputValues) {

        BasicCacheNetworkSimulation replication = null;
        int stat_size = 1;
        Statistic[] arrivalRate = new Statistic[stat_size];
        Statistic[] responseTime = new Statistic[stat_size];
        Statistic[] hitProb = new Statistic[stat_size];
        Statistic[] hitRate = new Statistic[stat_size];
        Statistic[] pitHitProb = new Statistic[stat_size];
        Statistic[] pitHitRate = new Statistic[stat_size];
        Statistic[] forwarding_rate = new Statistic[stat_size];
        Statistic[] forwarding_prob = new Statistic[stat_size];
        Statistic[] forwarding_number = new Statistic[stat_size];

        Statistic[] anal_hitProb = new Statistic[stat_size];
        Statistic[] anal_responseTime = new Statistic[stat_size];
        Statistic[] anal_hitRate = new Statistic[stat_size];
        Statistic[] anal_pitHitProb = new Statistic[stat_size];
        Statistic[] anal_pitHitRate = new Statistic[stat_size];
        Statistic[] anal_forwarding_rate = new Statistic[stat_size];
        Statistic[] anal_forwarding_prob = new Statistic[stat_size];
        Statistic[] anal_forwarding_number = new Statistic[stat_size];

        replication = new BasicCacheNetworkSimulation(Constants.REF.BasicCacheNetwork);

        for (int i = 0; i < iterationNum; i++) {

            replication.getCache(0).resetStatsitcis();
            replication.resetSimulationTime();
            RandomGen.resetSeed();

            if (replication.getSimulationMode()) {
                replication.startSimulation(basePath + fileName);
            }

            //for (int k = 0; k <= 1; k++) {
            for (int id = 0; id < stat_size; id++) {

                // Arrival Rate
                if (arrivalRate[id] == null) arrivalRate[id] = new Statistic(true);
                arrivalRate[id].addValue(replication.getCache(id).getTheoreticalArrivalRate());

                //hit number
                if (hitProb[id] == null) {
                    hitProb[id] = new Statistic(true);
                    anal_hitProb[id] = new Statistic(true);
                    hitRate[id] = new Statistic(true);
                    anal_hitRate[id] = new Statistic(true);
                }
                hitProb[id].addValue(replication.getCache(id).getHitProbability());
                anal_hitProb[id].addValue(replication.getCache(id).computeAnalyticHitProbability());
                hitRate[id].addValue(replication.getCache(id).getHitRate());
                anal_hitRate[id].addValue(replication.getCache(id).computeAnalyticHitRate());

                //pit and forwarding
                if (pitHitProb[id] == null) {
                    pitHitProb[id] = new Statistic(true);
                    anal_pitHitProb[id] = new Statistic(true);
                    pitHitRate[id] = new Statistic(true);
                    anal_pitHitRate[id] = new Statistic(true);
                    forwarding_prob[id] = new Statistic(true);
                    anal_forwarding_prob[id] = new Statistic(true);
                    forwarding_rate[id] = new Statistic(true);
                    anal_forwarding_rate[id] = new Statistic(true);
                    forwarding_number[id] = new Statistic(true);
                    anal_forwarding_number[id] = new Statistic(true);
                    responseTime[id] = new Statistic(true);
                    anal_responseTime[id] = new Statistic(true);
                }
                //pitLength[k].addValue(replication.getCache(k).getPitHitProbability());
                pitHitProb[id].addValue(replication.getCache(id).getPitHitProbability());
                anal_pitHitProb[id].addValue(replication.getCache(id).computeAnalyticPitHitProbability());
                pitHitRate[id].addValue(replication.getCache(id).getPITHitRate());
                anal_pitHitRate[id].addValue(replication.getCache(id).computeAnalyticPitHitRate());
                forwarding_rate[id].addValue(replication.getCache(id).getForwardingRate());
                anal_forwarding_rate[id].addValue(replication.getCache(id).computeAnalyticForwardingRate());
                forwarding_prob[id].addValue(replication.getCache(id).getForwardingProbability());
                anal_forwarding_prob[id].addValue(replication.getCache(id).computeAnalyticForwardingProbability());
                forwarding_number[id].addValue(replication.getCache(id).getForwardingNumber());
                anal_forwarding_number[id].addValue(replication.getCache(id).computeAnalyticForwardingNumber());
                anal_responseTime[id].addValue(replication.getCache(id).computeAnalyticResponseTime());
                responseTime[id].addValue(replication.getCache(id).getResponseTime());
            }
            System.out.println("Total Time: " + Double.toString(replication.getTotalSimulationTime()) + "\n");
        }

        for (int id = 0; id < stat_size; id++) {
            //Arrival
            double simulation = arrivalRate[id].getMean();

            outputValues.put(Constants.KEY.TOTAL_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.TOTAL_RATE).add(simulation);

            //Hit Prob
            double analytic = anal_hitProb[id].getMean();
            simulation = hitProb[id].getMean();
            outputValues.put(Constants.KEY.HIT, new LinkedList<Double>());
            outputValues.get(Constants.KEY.HIT).add(analytic);
            outputValues.get(Constants.KEY.HIT).add(simulation);
            outputValues.get(Constants.KEY.HIT).add(hitProb[id].getConfidenceInterval());

            //Hit Rate
            outputValues.put(Constants.KEY.HIT_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.HIT_RATE).add(anal_hitRate[id].getMean());
            outputValues.get(Constants.KEY.HIT_RATE).add(hitRate[id].getMean());
            outputValues.get(Constants.KEY.HIT_RATE).add(hitRate[id].getConfidenceInterval());

            //ResponseTime
            analytic = anal_responseTime[id].getMean();
            simulation = responseTime[id].getMean();
            outputValues.put(Constants.KEY.RESPONSE_TIME, new LinkedList<Double>());
            outputValues.get(Constants.KEY.RESPONSE_TIME).add(analytic);
            outputValues.get(Constants.KEY.RESPONSE_TIME).add(simulation);
            outputValues.get(Constants.KEY.RESPONSE_TIME).add(responseTime[id].getConfidenceInterval());


            //Pit Hit Prob
            analytic = anal_pitHitProb[id].getMean();
            simulation = pitHitProb[id].getMean();
            outputValues.put(Constants.KEY.PIT_HIT, new LinkedList<Double>());
            outputValues.get(Constants.KEY.PIT_HIT).add(analytic);
            outputValues.get(Constants.KEY.PIT_HIT).add(simulation);
            outputValues.get(Constants.KEY.PIT_HIT).add(pitHitProb[id].getConfidenceInterval());

            //Pit Hit Rate
            outputValues.put(Constants.KEY.PIT_HIT_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.PIT_HIT_RATE).add(anal_pitHitRate[id].getMean());
            outputValues.get(Constants.KEY.PIT_HIT_RATE).add(pitHitRate[id].getMean());
            outputValues.get(Constants.KEY.PIT_HIT_RATE).add(pitHitRate[id].getConfidenceInterval());

            //Forwarding Rate
            outputValues.put(Constants.KEY.FORWARDING_RATE, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_RATE).add(anal_forwarding_rate[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_RATE).add(forwarding_rate[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_RATE).add(forwarding_rate[id].getConfidenceInterval());

            //Forwarding Prob
            outputValues.put(Constants.KEY.FORWARDING_PROB, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_PROB).add(anal_forwarding_prob[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_PROB).add(forwarding_prob[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_PROB).add(forwarding_prob[id].getConfidenceInterval());

            //Forwarding Number
            outputValues.put(Constants.KEY.FORWARDING_NUMBER, new LinkedList<Double>());
            outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(anal_forwarding_number[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(forwarding_number[id].getMean());
            outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(forwarding_number[id].getConfidenceInterval());

            if (replication.isTraceBased() == 1) {
                outputValues.put(Constants.KEY.DENSITY, new LinkedList<Double>());
                outputValues.get(Constants.KEY.DENSITY).add(replication.getTraceDensity());

                outputValues.put(Constants.KEY.TRACE_CATLOG, new LinkedList<Double>());
                outputValues.get(Constants.KEY.TRACE_CATLOG).add(((double) replication.getTraceCatalogueSize()));
            }

        }
        System.out.println("Total Time: " + Double.toString(replication.getTotalSimulationTime()) + "\n");

        if (Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.PRINT_INDEX).intValue() >= 1) {
            //replication.getCache(0).printCatalogIndexStatistics(basePath + fileName);
            replication.getCache(0).printClassStatistics(basePath + fileName);

        }

    }

    public BasicCache getCache(int k) {
        return caches.get(k);
    }

    private void generateHypoEvents() {
        SimulationLogging.getLogger().info("Generate Events...");
        double time = 0;
        for (int f = 1; f <= popularityDis.getCatalogSize(); f++) {
            time = RandomGen.hypoExp(constants.get(Constants.KEY.Z).intValue(), popularityDis.getArrivalRate(f));
            RequestEvent requestEvent = new RequestEvent(f, 1, Constants.MESSAGE.REQUEST, time);
            addEvent(requestEvent);
            eventNum++;
        }
    }

    private void generateTraceEvents(int warmUp) throws FileNotFoundException {
        SimulationLogging.getLogger().info("Generate Events from the Trace...");
        File traceFile = new File("trace/trace.txt");
        if (warmUp == 0) {
            //real Phase
            trace = new Trace(traceFile, trimProb);
        } else {
            //warmUp phase
            trace = new Trace(traceFile, 0.0);
        }
        RequestEvent requestEvent = trace.readTrace();
        addEvent(requestEvent);
        eventNum++;

    }

    /**
     * executes the simulation
     */
    private void startSimulation(String filePath) {

        int warmUp = 0; //without warmup phase for the trace
        if (traceBased == 1) {
            try {
                generateTraceEvents(warmUp);
                warmUp = 1;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else generateHypoEvents();

        Event primitiveEvent = null;
        RequestEvent event = null;

        SimulationLogging.getLogger().info("----------------Start Simulation-------------------");

        while (hasMoreEvents() || warmUp == 0) {
            if (hasMoreEvents()) {
                primitiveEvent = pollNextEvent();
                SimulationLogging.getLogger().info(primitiveEvent.toString());
                assert primitiveEvent.getTriggerTime() >= clock;
                clock = primitiveEvent.getTriggerTime();
            } else if (traceBased == 1 && warmUp == 0) {
                getCache(0).resetStatsitcis();
                resetSimulationTime();
                try {
                    generateTraceEvents(warmUp);
                    warmUp = 1;
                    continue;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else {
                break;
            }

            if (true) {
                event = (RequestEvent) primitiveEvent;

                int cId = event.getCurrentCache();
                switch (event.getMessageType()) {

                    case EVICT:
                        caches.get(cId - 1).evictContent(event.getfId());
                        break;
                    case REQUEST:

                        if (traceBased == 0) {
                            if (eventNum < constants.get(Constants.KEY.NR_REQUEST).intValue()) {
                                //double time = RandomGen.hypoExp(constants.get(Constants.KEY.Z).intValue(), popularityDis.getArrivalRate());
                                //int fileId = caches.get(0).getPopularityDist().inverseCDF(RandomGen.uniform());
                                //RequestEvent requestEvent = new RequestEvent(fileId, 1, Constants.MESSAGE.REQUEST, time + clock);
                                double time = RandomGen.hypoExp(constants.get(Constants.KEY.Z).intValue(), popularityDis.getArrivalRate(event.getfId()));
                                RequestEvent requestEvent = new RequestEvent(event.getfId(), 1, Constants.MESSAGE.REQUEST, time + clock);
                                addEvent(requestEvent);
                                eventNum++;
                                if (eventNum % 2048 == 0) System.out.println("Progress: " + eventNum);
                                if (eventNum % 1000000 == 0) {
                                    try {
                                        //printStatistics( filePath, eventNum);
                                        Scanner doMoreJobs = new Scanner(new FileReader(filePath + "continue.txt"));
                                        int moreJobs = doMoreJobs.nextInt();
                                        System.out.println(moreJobs);
                                        if (moreJobs == 0) return;
                                        doMoreJobs.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            RequestEvent requestEvent = trace.readTrace();
//                            if(warmUp==1) {
//                                while (requestEvent == null ) {
//                                    requestEvent = trace.readTrace();
//                                }
//                            }
                            if (requestEvent != null) {
                                addEvent(requestEvent);
                                eventNum++;
                                if (eventNum % 2048 == 0) System.out.println("Progress: " + eventNum);
                            }
                        }
                        assert cId == 1;
                        caches.get(0).receiveMessage(event); //enqueu to the queue of the server
                        break;

                    case DATA:
                        caches.get(0).receiveMessage(event);
                        break;
                    default:
                        break;
                }
            }

        }

    }

    @Override
    public void download(RequestEvent event, double downloadTime) {
        event.setMessageType(Constants.MESSAGE.DATA);
        event.setTriggerTime(clock + downloadTime);
        addEvent(event);
    }

}
