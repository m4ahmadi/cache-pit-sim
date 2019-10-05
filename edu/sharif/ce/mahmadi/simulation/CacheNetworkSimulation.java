package edu.sharif.ce.mahmadi.simulation;

import edu.sharif.ce.mahmadi.caching.*;
import edu.sharif.ce.mahmadi.server.Event;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.RandomGen;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;
import edu.sharif.ce.mahmadi.utility.Statistic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class CacheNetworkSimulation extends Simulation {


    private ArrayList<Cache> caches; // array of caches
    private int eventNum;

    public CacheNetworkSimulation(Constants.REF ref) {
        super(ref);
        clock = 0; // time is zero
        eventNum = 0;
        generateServers();
    }


    private void generateAnalyticServers() {

        //Generate server for this simulation
        Constants.POLICY policy = Constants.POLICY.values()[constants.get(Constants.KEY.POLICY).intValue()];
        boolean pit = constants.get(Constants.KEY.PIT).intValue() >= 1;
        final double lambda = constants.get(Constants.KEY.REQUEST_RATE).doubleValue();
        double service_rate = constants.get(Constants.KEY.SERVICE_RATE).doubleValue();
        double data_service_rate = constants.get(Constants.KEY.DATA_SERVICE_RATE).doubleValue();
        double write_service_rate = constants.get(Constants.KEY.WRITE_RATE).doubleValue();
        double download_rate = constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        Constants.SCHEDULING scheduling = Constants.SCHEDULING.values()[constants.get(Constants.KEY.SCHEDULING).intValue()];
        Constants.DIST cacheDist = Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()];
        double zipf = constants.get(Constants.KEY.ZIPF).doubleValue();
        int catalog_size = constants.get(Constants.KEY.F).intValue();
        int cache_size = (int) (constants.get(Constants.KEY.C).doubleValue() * catalog_size);
        int flowNum = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.FLOW_NUM).intValue();



        Cache cache = null;
        switch (policy) {
            case TTL:
                //cache = new TTLCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case R_TTL:
                //cache = new RenewableTTLCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case LRU:
                cache = new LRUCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case FIFO:
                //cache = new FIFOCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            default:
                break;
        }

        double hit = constants.get(Constants.KEY.HIT_PROB).doubleValue();
        //cache = new CacheEmulator(hit, Constants.SCHEDULING.PS, constants.get(Constants.KEY.PIT).intValue() >= 1, 1, lambda * (2 - hit), constants.get(Constants.KEY.SERVICE_RATE).doubleValue(), constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue(), Constants.DIST.CONST, this, constants.get(Constants.KEY.ZIPF).doubleValue(), constants.get(Constants.KEY.F).intValue());
        Cache download = new CacheEmulator(1, Constants.SCHEDULING.IS, pit, 2, new double[]{lambda * (1 - hit)}, new double[]{download_rate}, 0, Constants.DIST.EXPO, this, zipf, catalog_size);
        Cache storage = new CacheEmulator(1, scheduling, pit, 3, new double[]{lambda * (1 - hit)}, new double[]{write_service_rate}, 0, Constants.DIST.EXPO, this, zipf, catalog_size);

        caches = new ArrayList<>();
        caches.add(cache);//1
        caches.add(download); //2
        caches.add(storage); //3
    }

    public static void RUN() {

        // creating new file
        String basePath = new String();
        File statsDir = null;

        Constants.POLICY policy = Constants.POLICY.values()[Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.POLICY).intValue()];
        Constants.SCHEDULING scheduling = Constants.SCHEDULING.values()[Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.SCHEDULING).intValue()];

        statsDir = new File(policy.toString() + "-" + scheduling + "-Dir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;
        //singleExecution(basePath);
        multiExecution(basePath);
    }


    private static void multiExecution(String basePath) {
        DecimalFormat df = new DecimalFormat("#.000000");
        /**
         * 0     -> 0 ms
         * 1e-5 -> 100ms -> 1e5 mics
         * 1e-3 -> 1ms -> 1000mics
         * 1e-4 -> 10ms -> 1e4 mics
         * 0.001 -> 1ms    -> 1000 mics
         * 0.01  -> 0.1ms  -> 100 mics
         * 0.1   -> 0.01ms -> 10 mics
         *
         *
         * 0, 10, 4
         * 0.1, 0.01, 0.001, 0.0001
         * 0, 1e-3/10, 1e-3/50, 1e-3/100
         * 2e-4, 2e-5, 1e-5, 1e-5, 66e-7, 5e-6
         * 1.0/6000.0,1.0/7000.0, 1.0/8000.0, 1.0/9000.0 good but difference with ZPD is low
         * 1.0/10, 1.0/50, 1.0/100, 1.0 / 200, 1.0 / 300, 1.0 / 400, 1.0 / 500, 1.0 /750, 1.0 /1000
         */
        double[] downloadRates = {0, 1.0 / 0.05, 1.0 / 0.1, 1.0 / 0.15, 1.0 / 0.2, 1.0 / 0.25};
        double request_rate = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.REQUEST_RATE).doubleValue();
        double service_rate = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.SERVICE_RATE).doubleValue();
        double data_service_rate = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.DATA_SERVICE_RATE).doubleValue();
        int iterationNum = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.ITERATION_NUM).intValue();
        int catalogSize = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.F).intValue();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        //long heapMaxSize = Runtime.getRuntime().maxMemory();
        //System.out.println(heapMaxSize);

        try {
            String fileName = "[" + request_rate + "-" + service_rate + "-" + data_service_rate + "-";
            StringBuilder results = new StringBuilder();
            FileWriter stats = new FileWriter(basePath + fileName + "totalResultFile.txt");
            Map<Constants.KEY, List<Double>> outputValues = null;
            Constants.KEY[] output_names = {Constants.KEY.TOTAL_RATE, Constants.KEY.UTILIZATION, Constants.KEY.HIT, Constants.KEY.RESPONSE_TIME, Constants.KEY.PIT_HIT, Constants.KEY.FORWARDING_RATE, Constants.KEY.FORWARDING_PROB, Constants.KEY.FORWARDING_NUMBER};
            results.append(Constants.KEY.DOWNLOAD_RATE.toString() + "\t");
            for (Constants.KEY output : output_names) {
                results.append(output.toString() + "\t");
            }
            results.append("\n");

            for (int index = 1; index <= downloadRates.length; index++) {
                outputValues = new HashMap<Constants.KEY, List<Double>>();
                // execute
                Constants.CONSTS.get(Constants.REF.Queue).put(Constants.KEY.DOWNLOAD_RATE, downloadRates[index - 1]);
                singleExecution(basePath, fileName + downloadRates[index - 1] + "-", iterationNum, catalogSize, outputValues);
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

    private static void singleExecution(String basePath, String fileName, int iterationNum, int catalogSize, Map<Constants.KEY, List<Double>> outputValues) {

        CacheNetworkSimulation replication = null;
        DecimalFormat df = new DecimalFormat("#.000000");
        int flowNum = 3;

        Statistic[] serviceRate = new Statistic[flowNum + 1];
        Statistic[] arrivalRate = new Statistic[flowNum + 1];
        Statistic[] waitingTime = new Statistic[flowNum + 1];
        Statistic[] responseTime = new Statistic[flowNum + 1];
        Statistic[] totalResponseTime = new Statistic[3];
        Statistic[] downloadTime = new Statistic[3];
        Statistic[] systemLength = new Statistic[flowNum + 1];
        Statistic[] utilization = new Statistic[flowNum + 1];
        Statistic[] hitProb = new Statistic[3];
        Statistic[] pitLength = new Statistic[3];
        Statistic[] cacheLength = new Statistic[3];
        Statistic[] pitHitProb = new Statistic[3];
        Statistic[] forwarding_rate = new Statistic[3];
        Statistic[] forwarding_prob = new Statistic[3];
        Statistic[] forwarding_number = new Statistic[3];

        Statistic[] anal_totalResponseTime = new Statistic[3];
        Statistic[] anal_systemLength = new Statistic[flowNum + 1];
        Statistic[] anal_waitingTime = new Statistic[flowNum + 1];
        Statistic[] anal_responseTime = new Statistic[flowNum + 1];
        Statistic[] anal_hitProb = new Statistic[3];
        Statistic[] anal_pitHitProb = new Statistic[3];
        Statistic[] anal_forwarding_rate = new Statistic[3];
        Statistic[] anal_forwarding_prob = new Statistic[3];
        Statistic[] anal_forwarding_number = new Statistic[3];
        Statistic[] anal_cacheLength = new Statistic[3];

        /*Statistic[] index_hitProb = new Statistic[catalogSize];
        Statistic[] index_responseTime = new Statistic[catalogSize];
        Statistic[] index_PITProb = new Statistic[catalogSize];
        Statistic[] anal_index_hitProb = new Statistic[catalogSize];
        Statistic[] anal_index_responseTime = new Statistic[catalogSize];
        Statistic[] anal_index_PITProb = new Statistic[catalogSize];
        for (int index = 0; index < catalogSize; index++) {
            anal_index_hitProb[index] = new Statistic();
            anal_index_responseTime[index] = new Statistic();
            anal_index_PITProb[index] = new Statistic();
            index_hitProb[index] = new Statistic();
            index_PITProb[index] = new Statistic();
            index_responseTime[index] = new Statistic();
        }*/

        try {
            FileWriter stats = new FileWriter(basePath + fileName + "detailedResults.txt");
            int k = 0;
            for (int i = 0; i < iterationNum; i++) {

                replication = new CacheNetworkSimulation(Constants.REF.Queue);
                //replication.startSimulation(basePath + fileName);
                k = 0;


                //for (int k = 0; k <= 1; k++) {
                for (int fId = 0; fId <= flowNum; fId++) {
                    if (fId == 3) {
                        k = 2;
                    }

                    // Arrival Rate
                    if (arrivalRate[fId] == null) arrivalRate[fId] = new Statistic(true);
                    arrivalRate[fId].addValue(replication.getCache(k).getArrivalRate(k == 2 ? 1 : fId));

                    if (fId != 0) {
                        // Service Rate
                        if (serviceRate[fId] == null) serviceRate[fId] = new Statistic(true);
                        serviceRate[fId].addReplication(replication.getCache(k).getServiceRate(k == 2 ? 1 : fId));
                    }
                    //Waiting Time
                    if (waitingTime[fId] == null) {
                        waitingTime[fId] = new Statistic(true);
                        anal_waitingTime[fId] = new Statistic(true);
                    }
                    waitingTime[fId].addReplication(replication.getCache(k).getServerWaitingTime(k == 2 ? 1 : fId));
                    anal_waitingTime[fId].addValue(replication.getCache(k).computeAnalyticServerWaitingTime(k == 2 ? 1 : fId));

                    //response time of server
                    if (responseTime[fId] == null) {
                        responseTime[fId] = new Statistic(true);
                        anal_responseTime[fId] = new Statistic(true);
                    }
                    responseTime[fId].addReplication(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId));
                    anal_responseTime[fId].addValue(replication.getCache(k).computeAnalyticServerResponseTime(k == 2 ? 1 : fId));

                    //System Length
                    if (systemLength[fId] == null) {
                        systemLength[fId] = new Statistic(true);
                        anal_systemLength[fId] = new Statistic(true);
                    }
                    systemLength[fId].addValue(replication.getCache(k).getAverageSystemLength(k == 2 ? 1 : fId));
                    anal_systemLength[fId].addValue(replication.getCache(k).computeAnalyticSystemLength(k == 2 ? 1 : fId));

                    //Utilization
                    if (utilization[fId] == null) utilization[fId] = new Statistic(true);
                    utilization[fId].addValue(replication.getCache(k).getUtilization(k == 2 ? 1 : fId));
                }

                for (k = 0; k <= 2; k++) {

                    //download time
//                    if (downloadTime[k] == null) downloadTime[k] = new Statistic(true);
//                    downloadTime[k].addValue(replication.getCache(k).getDownloadTime());

                    //ResponseTime
                    if (totalResponseTime[k] == null) {
                        totalResponseTime[k] = new Statistic(true);
                        anal_totalResponseTime[k] = new Statistic(true);
                    }
//                    totalResponseTime[k].addReplication(replication.getCache(k).getResponseTime());
                     anal_totalResponseTime[k].addValue(replication.getCache(k).computeAnalyticResponseTime());

                    //hit number
                    if (hitProb[k] == null) {
                        hitProb[k] = new Statistic(true);
                        anal_hitProb[k] = new Statistic(true);
                    }
                    hitProb[k].addValue(replication.getCache(k).getHitProbability());
                    anal_hitProb[k].addValue(replication.getCache(k).computeAnalyticHitProbability());

                    //pit Length
                    if (pitHitProb[k] == null) {
                        //pitLength[k] = new Statistic(true);
                        pitHitProb[k] = new Statistic(true);
                        anal_pitHitProb[k] = new Statistic(true);
                        forwarding_prob[k] = new Statistic(true);
                        anal_forwarding_prob[k] = new Statistic(true);
                        forwarding_rate[k] = new Statistic(true);
                        anal_forwarding_rate[k] = new Statistic(true);
                        forwarding_number[k] = new Statistic(true);
                        anal_forwarding_number[k] = new Statistic(true);
                    }
                    //pitLength[k].addValue(replication.getCache(k).getPitHitProbability());
                    pitHitProb[k].addValue(replication.getCache(k).getPitHitProbability());
                    anal_pitHitProb[k].addValue(replication.getCache(k).computeAnalyticPitHitProbability());
                    forwarding_rate[k].addValue(replication.getCache(k).getForwardingRate());
                    anal_forwarding_rate[k].addValue(replication.getCache(k).computeAnalyticForwardingRate());
                    forwarding_prob[k].addValue(replication.getCache(k).getForwardingProbability());
                    anal_forwarding_prob[k].addValue(replication.getCache(k).computeAnalyticForwardingProbability());
                    forwarding_number[k].addValue(replication.getCache(k).getForwardingNumber());
                    anal_forwarding_number[k].addValue(replication.getCache(k).computeAnalyticForwardingNumber());
                    //anal_pitHitProb[k].addValue(replication.getCache(k).computeAnalyticExpectedPITSize());

                    //cache Length
                    if (cacheLength[k] == null) {
                        cacheLength[k] = new Statistic(true);
                        anal_cacheLength[k] = new Statistic(true);
                    }
                    //acheLength[k].addValue(replication.getCache(k).getAverageCacheLength());
                    //anal_cacheLength[k].addValue(replication.getCache(k).computeAnalyticExpectedCacheSize());
                }
            }

            stats.write("Analytic\tSimulation\tCI\tAbsoluteErr\tRelativeErr\n");

            k = 0;
            //printIndexStatistics(basePath + fileName, catalogSize, replication.getCache(k).getPopularityDist(), replication.getCache(k).getLambda(1), index_hitProb, anal_index_hitProb, index_responseTime, anal_index_responseTime, index_PITProb, anal_index_PITProb);
            for (int fId = 0; fId <= flowNum; fId++) {

                if (fId == 3) {
                    k = 2;
                }
                //Arrival
                double analytic = (fId == 2) ? replication.getCache(k).computeAnalyticForwardingRate() : (fId == 1) ? replication.caches.get(k).getLambda(fId) : (replication.getCache(k).computeAnalyticForwardingRate() + replication.caches.get(k).getLambda(1));
                double simulation = arrivalRate[fId].getMean();
                double absoluteError = Math.abs(analytic - simulation);
                double relError = analytic != 0 ? (absoluteError / analytic) : 0;
                if (fId == 0) {
                    outputValues.put(Constants.KEY.TOTAL_RATE, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.TOTAL_RATE).add(analytic);
                    outputValues.get(Constants.KEY.TOTAL_RATE).add(simulation);
                    outputValues.get(Constants.KEY.TOTAL_RATE).add(arrivalRate[fId].getConfidenceInterval());
                    outputValues.get(Constants.KEY.TOTAL_RATE).add(absoluteError);
                    outputValues.get(Constants.KEY.TOTAL_RATE).add(relError);
                }
                stats.write(String.format("ArrivalRate\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(arrivalRate[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                if (fId != 0) {
                    //ServiceRate
                    analytic = replication.getCache(k).getMiu(k == 2 ? 1 : fId);
                    simulation = 1 / serviceRate[fId].getMean();
                    absoluteError = Math.abs(analytic - simulation);
                    relError = analytic != 0 ? (absoluteError / analytic) : 0;
                    stats.write(String.format("ServiceRate\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(serviceRate[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));
                }
                //waiting time of server
                analytic = anal_waitingTime[fId].getMean();
                simulation = waitingTime[fId].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("WaitingTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(waitingTime[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //response time of server
                analytic = anal_responseTime[fId].getMean();
                simulation = responseTime[fId].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("ResponseTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(responseTime[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));
                stats.write(String.format("MaxResponseTime\t%s\t%s\t%s\t%s\t%s\n", df.format(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId).getMin()), df.format(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId).getNormalPecentile(25)), df.format(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId).getSTD()), df.format(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId).getNormalPecentile(75)), df.format(replication.getCache(k).getServerResponseTime(k == 2 ? 1 : fId).getMax())));

                //system length
                analytic = anal_systemLength[fId].getMean();
                simulation = systemLength[fId].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("SystemLength\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(systemLength[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //utilization
                analytic = replication.getCache(k).computeAnalyticUtilization(k == 2 ? 1 : fId);
                simulation = utilization[fId].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = (analytic != 0) ? (absoluteError / analytic) : 0;
                if (fId == 0) {
                    outputValues.put(Constants.KEY.UTILIZATION, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.UTILIZATION).add(analytic);
                    outputValues.get(Constants.KEY.UTILIZATION).add(simulation);
                    outputValues.get(Constants.KEY.UTILIZATION).add(utilization[fId].getConfidenceInterval());
                    outputValues.get(Constants.KEY.UTILIZATION).add(absoluteError);
                    outputValues.get(Constants.KEY.UTILIZATION).add(relError);
                }
                stats.write(String.format("Utilization\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(utilization[fId].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));
            }
            for (k = 0; k <= 2; k++) {
                //download time
                double analytic = replication.getCache(k).getTheoreticalDownTime();
                double simulation = downloadTime[k].getMean();
                double absoluteError = Math.abs(analytic - simulation);
                double relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("DownloadTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(downloadTime[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //response time
                analytic = anal_totalResponseTime[k].getMean();
                simulation = totalResponseTime[k].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                if (k == 0) {
                    outputValues.put(Constants.KEY.RESPONSE_TIME, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(analytic);
                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(simulation);
                   outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTime());
//                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(25));
//                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(50));
//                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(75));
//                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(80));
//                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(90));
                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(replication.getCache(k).getResponseTime());

                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(absoluteError);
                    outputValues.get(Constants.KEY.RESPONSE_TIME).add(relError);
                }
                stats.write(String.format("TResponseTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(totalResponseTime[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));
                //stats.write(String.format("MaxTResponseTime\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", df.format(replication.getCache(k).getResponseTimeStatistic().getMin()), df.format(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(25)), df.format(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(50)), df.format(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(75)), df.format(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(80)), df.format(replication.getCache(k).getResponseTimeStatistic().getNormalPecentile(90)), df.format(replication.getCache(k).getResponseTimeStatistic().getMax())));


                //Hit Prob
                analytic = anal_hitProb[k].getMean();
                simulation = hitProb[k].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                if (k == 0) {
                    outputValues.put(Constants.KEY.HIT, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.HIT).add(analytic);
                    outputValues.get(Constants.KEY.HIT).add(simulation);
                    outputValues.get(Constants.KEY.HIT).add(hitProb[k].getConfidenceInterval());
                    outputValues.get(Constants.KEY.HIT).add(absoluteError);
                    outputValues.get(Constants.KEY.HIT).add(relError);
                }
                stats.write(String.format("HitProb\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(hitProb[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //Pit Length
               /* analytic = anal_pitHitProb[k].getMean();
                simulation = pitLength[k].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("PITLength\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(pitLength[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));
                */
                //Pit Hit Prob
                analytic = anal_pitHitProb[k].getMean();
                simulation = pitHitProb[k].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                if (k == 0) {
                    outputValues.put(Constants.KEY.PIT_HIT, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.PIT_HIT).add(analytic);
                    outputValues.get(Constants.KEY.PIT_HIT).add(simulation);
                    outputValues.get(Constants.KEY.PIT_HIT).add(hitProb[k].getConfidenceInterval());
                    outputValues.get(Constants.KEY.PIT_HIT).add(absoluteError);
                    outputValues.get(Constants.KEY.PIT_HIT).add(relError);

                    outputValues.put(Constants.KEY.FORWARDING_RATE, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.FORWARDING_RATE).add(anal_forwarding_rate[k].getMean());
                    outputValues.get(Constants.KEY.FORWARDING_RATE).add(forwarding_rate[k].getMean());

                    outputValues.put(Constants.KEY.FORWARDING_PROB, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.FORWARDING_PROB).add(anal_forwarding_prob[k].getMean());
                    outputValues.get(Constants.KEY.FORWARDING_PROB).add(forwarding_prob[k].getMean());

                    outputValues.put(Constants.KEY.FORWARDING_NUMBER, new LinkedList<Double>());
                    outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(anal_forwarding_number[k].getMean());
                    outputValues.get(Constants.KEY.FORWARDING_NUMBER).add(forwarding_number[k].getMean());

                }
                stats.write(String.format("PITHitProb\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(pitHitProb[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //Cache Length
                analytic = anal_cacheLength[k].getMean();
                simulation = cacheLength[k].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = analytic != 0 ? (absoluteError / analytic) : 0;
                stats.write(String.format("CacheLength\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(cacheLength[k].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

            }
            stats.write(Double.toString(replication.getCache(0).getTotalTime()) + "\n");
            stats.write(Double.toString(replication.getCache(0).getTotalArrivalTime()) + "\n");
            stats.write(Double.toString(replication.getCache(0).getCTime()) + "\n");
            stats.flush();
            stats.close();
            //replication.getCache(0).getResponseTimeStatistic().printStatistics(basePath + fileName + "t-responseTimePdf.txt", basePath +fileName+ "t-responseTimesCdf.txt");

            //if (Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.PRINT_INDEX).intValue() >= 1) {
                replication.getCache(0).formatCatalogHitProbability(basePath + fileName);
                //replication.getCache(0).getPitLengthStatistic().printPdf(basePath + fileName + "-pitLengthPdf.txt", replication.getCache(0).getTotalTime());
                //replication.getCache(0).getCacheLengthStatistic().printPdf(basePath + fileName + "-cacheLengthPdf.txt", replication.getCache(0).getTotalTime());
            //}

            if (false) {

                replication.getCache(0).getServerInterRequestTimeStamps().printStatistics(basePath + "InterRequestsPdfCache.txt", basePath + "InterRequestsCdfCache.txt");
                replication.getCache(1).getServerInterRequestTimeStamps().printStatistics(basePath + "InterRequestsPdf.txt", basePath + "InterRequestsCdf.txt");
                //replication.getCache(0).getResponseTimeStatistic().printStatistics(basePath + "t-responseTimePdf.txt", basePath + "t-responseTimesCdf.txt");
                replication.getCache(0).getServerResponseTime(1).printStatistics(basePath + "s-responseTimePdf.txt", basePath + "s-responseTimesCdf.txt");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printIndexStatistics(String fileName, int catalogSize, Dist popularityDist, double lambda, Statistic[] index_hitProb, Statistic[] anal_index_hitProb, Statistic[] index_ResponseTime, Statistic[] anal_index_responseTime, Statistic[] index_pitProb, Statistic[] anal_index_pitProb) {
        try {
            FileWriter stats = new FileWriter(fileName + "-index.txt");
            StringBuilder st = new StringBuilder();
            st.append("Prob\tRate\tAHit\tSHit\tCI\tAW\tSW\tCI\tACL\tSCL\tCI\tAPIT\tSPIT\tCI\n");
            for (int f = 0; f < catalogSize; f++) {
                st.append((f + 1) + "\t" + popularityDist.getPDF(f + 1) + "\t" + popularityDist.getPDF(f + 1) * lambda + "\t" + anal_index_hitProb[f].getMean() + "\t" + anal_index_hitProb[f].getConfidenceInterval() + "\t" + index_hitProb[f].getMean() + "\t" + index_hitProb[f].getConfidenceInterval() + "\t" + anal_index_responseTime[f].getMean() + "\t" + anal_index_responseTime[f].getConfidenceInterval() + "\t" + index_ResponseTime[f].getMean() + "\t" + index_ResponseTime[f].getConfidenceInterval() + "\t" + anal_index_pitProb[f].getMean() + "\t" + anal_index_pitProb[f].getConfidenceInterval() + "\t" + index_pitProb[f].getMean() + "\t" + index_pitProb[f].getConfidenceInterval() + "\n");
            }
            stats.write(st.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateServers() {
        //Generate server for this simulation

        Constants.POLICY policy = Constants.POLICY.values()[constants.get(Constants.KEY.POLICY).intValue()];
        boolean pit = constants.get(Constants.KEY.PIT).intValue() >= 1;
        final double lambda = constants.get(Constants.KEY.REQUEST_RATE).doubleValue();
        double service_rate = constants.get(Constants.KEY.SERVICE_RATE).doubleValue();
        double data_service_rate = constants.get(Constants.KEY.DATA_SERVICE_RATE).doubleValue();
        double write_service_rate = constants.get(Constants.KEY.WRITE_RATE).doubleValue();
        double download_rate = constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue();
        Constants.SCHEDULING scheduling = Constants.SCHEDULING.values()[constants.get(Constants.KEY.SCHEDULING).intValue()];
        Constants.DIST cacheDist = Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()];
        double zipf = constants.get(Constants.KEY.ZIPF).doubleValue();
        int catalog_size = constants.get(Constants.KEY.F).intValue();
        int cache_size = (int) (constants.get(Constants.KEY.C).doubleValue() * catalog_size);
        int flowNum = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.FLOW_NUM).intValue();


        Cache cache = null;
        switch (policy) {
            case TTL:
                //cache = new TTLCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case R_TTL:
                //cache = new RenewableTTLCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case LRU:
                cache = new LRUCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            case FIFO:
                //cache = new FIFOCache(cache_size, scheduling, pit, 1, new double[]{lambda, 15}, new double[]{service_rate, data_service_rate}, download_rate, cacheDist, this, zipf, catalog_size);
                break;
            default:
                break;
        }

        double hit = constants.get(Constants.KEY.HIT_PROB).doubleValue();
        //cache = new CacheEmulator(hit, Constants.SCHEDULING.PS, constants.get(Constants.KEY.PIT).intValue() >= 1, 1, lambda * (2 - hit), constants.get(Constants.KEY.SERVICE_RATE).doubleValue(), constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue(), Constants.DIST.CONST, this, constants.get(Constants.KEY.ZIPF).doubleValue(), constants.get(Constants.KEY.F).intValue());
        Cache download = new CacheEmulator(1, Constants.SCHEDULING.IS, pit, 2, new double[]{lambda * (1 - hit)}, new double[]{download_rate}, 0, Constants.DIST.EXPO, this, zipf, catalog_size);
        Cache storage = new CacheEmulator(1, scheduling, pit, 3, new double[]{lambda * (1 - hit)}, new double[]{write_service_rate}, 0, Constants.DIST.EXPO, this, zipf, catalog_size);

        caches = new ArrayList<>();
        caches.add(cache);//1
        caches.add(download); //2
        caches.add(storage); //3
    }

    /**
     * generates update and request events for each server
     */
    private void generateEvents() {
        SimulationLogging.getLogger().info("Generate Events...");
        double time = 0;
        double rate = constants.get(Constants.KEY.REQUEST_RATE).doubleValue(); //rate of arrival of events
        for (int r = 1; r <= 1; r++) {
            time += RandomGen.exp(rate);
            int fileId = caches.get(0).getPopularityDist().inverseCDF(RandomGen.uniform());
            RequestEvent requestEvent = new RequestEvent(fileId, 1, Constants.EVENT_TYPE.ARRIVAL, Constants.MESSAGE.REQUEST, time);
            addEvent(requestEvent);
            //requestEvent.setDeadline(generateDeadline(requestEvent.getFlowId())); //set the deadline for this job
            //if(requestEvent.getDeadline() < Double.MAX_VALUE) {
            //    DeadlineEvent deadlineEvent = new DeadlineEvent(Constants.EVENT_TYPE.DEADLINE,time + requestEvent.getDeadline(), requestEvent);
            //    addEvent(deadlineEvent);
            //}
            eventNum++;
        }
    }

    // Return the server based ont Id
    public Cache getCache(int i) {
        return caches.get(i);
    }

    /**
     * executes the simulation
     */
    private void startSimulation(String filePath) {
        generateEvents();
        Event primitiveEvent = null;
        RequestEvent event = null;
        //DeadlineEvent deadline= null;

        SimulationLogging.getLogger().info("----------------Start Simulation-------------------");

        while (hasMoreEvents()) {
            primitiveEvent = pollNextEvent();
            //SimulationLogging.getLogger().info(event.toString());
            assert primitiveEvent.getTriggerTime() >= clock;
            clock = primitiveEvent.getTriggerTime();

//            if(primitiveEvent.getType().equals(Constants.EVENT_TYPE.DEADLINE)){
//                deadline = (DeadlineEvent) primitiveEvent;
//                if(!deadline.getMainEvent().isFinished()) {
//                    removeCorrespondingEvent(deadline.getMainEvent());
//                }
//            }
            if (true) {
                event = (RequestEvent) primitiveEvent;
                int cId = event.getCurrentCache();
                switch (event.getType()) {

                    case EVICT:
                        caches.get(cId - 1).evictContent(event.getfId(), clock);
                        break;
                    case ARRIVAL:
                        if (eventNum < constants.get(Constants.KEY.NR_REQUEST).intValue()) {
                            double time = RandomGen.exp(constants.get(Constants.KEY.REQUEST_RATE).doubleValue());
                            int fileId = caches.get(0).getPopularityDist().inverseCDF(RandomGen.uniform());
                            RequestEvent requestEvent = new RequestEvent(fileId, 1, Constants.EVENT_TYPE.ARRIVAL, Constants.MESSAGE.REQUEST, time + clock);
//                            requestEvent.setDeadline(generateDeadline(requestEvent.getFlowId())); //set the deadline for this job
                            addEvent(requestEvent);
//                            if (requestEvent.getDeadline() < Double.MAX_VALUE) {
//                                DeadlineEvent deadlineEvent = new DeadlineEvent(Constants.EVENT_TYPE.DEADLINE, clock + time + requestEvent.getDeadline(), requestEvent);
//                                addEvent(deadlineEvent);
//                            }
                            eventNum++;
                            if (eventNum % 2048 == 0) System.out.println("Progress: " + eventNum);
                            if (eventNum % 1e6 == 0) {
                                try {
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
                        assert cId == 1;
                        caches.get(0).receiveMessage(event, clock); //enqueu to the queue of the server
                        break;

                    case DEPARTURE:
                        if (cId == 1) {
                            caches.get(0).processMessage(event, clock);
                            // if this is the data downloaded, it should be forwarded to the cache storage!
                            if (event.getMessageType() == Constants.MESSAGE.DATA) {
                                event.setCurrentCache(3);
                                event.setMessageType(Constants.MESSAGE.REQUEST);
                                event.setBirthTime(clock);
                                event.setType(Constants.EVENT_TYPE.ARRIVAL);
                                caches.get(2).receiveMessage(event, clock); //to write the content
                            }
                        } else if (cId == 2) {
                            //downloads the content and passes that to the cache index table for writing
                            caches.get(1).processMessage(event, clock);
                            event.setCurrentCache(1);
                            event.setMessageType(Constants.MESSAGE.DATA);
                            event.setBirthTime(clock);
                            event.setType(Constants.EVENT_TYPE.ARRIVAL);
                            caches.get(0).receiveMessage(event, clock);
                        } else if (cId == 3) {
                            caches.get(2).processMessage(event, clock);
                            event.setMessageType(Constants.MESSAGE.WRITE);
                            event.setCurrentCache(1);
                            caches.get(0).processMessage(event, clock);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }

    @Override
    public void requestFromOthers(RequestEvent request) {
        request.setCurrentCache(2);
        request.setBirthTime(clock);
        request.setType(Constants.EVENT_TYPE.ARRIVAL);
        caches.get(1).receiveMessage(request, clock);
    }

}

