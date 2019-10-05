package edu.sharif.ce.mahmadi.simulation;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.server.FCFSServer;
import edu.sharif.ce.mahmadi.server.ISServer;
import edu.sharif.ce.mahmadi.server.Server;
import edu.sharif.ce.mahmadi.utility.RandomGen;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;
import edu.sharif.ce.mahmadi.utility.Statistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class QueueNetworkSimulation extends Simulation {


    private ArrayList<Server> servers; // array of caches
    private Server cache;
    private Server download;
    private int hitNum;
    private int eventNum;


    public QueueNetworkSimulation(Constants.REF ref) {
        super(ref);
        clock = 0; // time is zero
        hitNum = 0;
        eventNum = 0;
        generateServers();
    }

    public static void RUN() {

        // creating new file
        String basePath = new String();
        File statsDir = null;

        statsDir = new File("QueueSimulatorDir");
        statsDir.mkdir();
        basePath = statsDir.getAbsolutePath() + File.separator;

        singleExecution(basePath);

    }

    private static void singleExecution(String basePath) {

        QueueNetworkSimulation replication = null;
        DecimalFormat df = new DecimalFormat("#.000000");

        Statistic[] serviceRate = new Statistic[2];
        Statistic[] arrivalRate = new Statistic[2];
        Statistic[] waitingTime = new Statistic[2];
        Statistic[] responseTime = new Statistic[2];
        Statistic[] systemLength = new Statistic[2];
        Statistic[] utilization = new Statistic[2];
        Statistic hitProb = new Statistic(true);


        try {
            FileWriter stats = new FileWriter(basePath + "QueueSimulation" + "stats.txt");
            int iterationNum = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.ITERATION_NUM).intValue();
            for (int i = 0; i < iterationNum; i++) {
                replication = new QueueNetworkSimulation(Constants.REF.Queue);
                replication.startSimulation();

                for (int k = 0; k <= 1; k++) {
                    // Arrival Rate
                    if (arrivalRate[k] == null) arrivalRate[k] = new Statistic(true);
                    arrivalRate[k].addValue(replication.getServer(k).getArrivalRate(1));

                    // Service Rate
                    if (serviceRate[k] == null) serviceRate[k] = new Statistic(true);
                    serviceRate[k].addReplication(replication.getServer(k).getServiceRate(1));

                    //Waiting Time
                    if (waitingTime[k] == null) waitingTime[k] = new Statistic(true);
                    waitingTime[k].addReplication(replication.getServer(k).getWaitingTime(1));

                    //ResponseTime
                    if (responseTime[k] == null) responseTime[k] = new Statistic(true);
                    responseTime[k].addReplication(replication.getServer(k).getResponseTime(1));

                    //System Length
                    if (systemLength[k] == null) systemLength[k] = new Statistic(true);
                    systemLength[k].addValue(replication.getServer(k).getAverageSystemLength(1));

                    //Utilization
                    if (utilization[k] == null) utilization[k] = new Statistic(true);
                    utilization[k].addValue(replication.getServer(k).getUtilization(1));

                    //hit number
                    hitProb.addValue(replication.getHitProbability());
                }
            }

            stats.write("Analytic\tSimulation\tCI\tAbsoluteErr\tRelativeErr\n");

            for (int i = 0; i <= 1; i++) {

                //Arrival
                double analytic = replication.getServer(i).getLambda(1);
                double simulation = arrivalRate[i].getMean();
                double absoluteError = Math.abs(analytic - simulation);
                double relError = absoluteError / analytic;
                stats.write(String.format("ArrivalRate\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(arrivalRate[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //ServiceRate
                analytic = replication.getServer(i).getMiu(1);
                simulation = 1 / serviceRate[i].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("ServiceRate\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(serviceRate[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //waiting time
                analytic = replication.getServer(i).computeAnalyticWaitingTime(1);
                simulation = waitingTime[i].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("WaitingTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(waitingTime[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //response time
                analytic = replication.getServer(i).computeAnalyticResponseTime(1);
                simulation = responseTime[i].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("ResponseTime\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(responseTime[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //utilization
                analytic = replication.getServer(i).computeAnalyticUtilization(1);
                simulation = utilization[i].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("Utilization\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(utilization[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //system length
                analytic = replication.getServer(i).computeAnalyticSystemLength(1);
                simulation = systemLength[i].getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("SystemLength\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(systemLength[i].getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

                //Hit Prob
                analytic = replication.getAnalyticHitProbability();
                simulation = hitProb.getMean();
                absoluteError = Math.abs(analytic - simulation);
                relError = absoluteError / analytic;
                stats.write(String.format("HitProb\t%s\t%s\t%s\t%s\t%s\n", df.format(analytic), df.format(simulation), df.format(hitProb.getConfidenceInterval()), df.format(absoluteError), df.format(relError)));

            }
            if (false) {
                replication.getServer(0).getInterRequestTimeStamps().printStatistics(basePath + "InterRequestsPdfCache.txt", basePath + "InterRequestsCdfCache.txt");
                replication.getServer(1).getInterRequestTimeStamps().printStatistics(basePath + "InterRequestsPdf.txt", basePath + "InterRequestsCdf.txt");
            }
            stats.flush();
            stats.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getHitProbability() {
        return ((double) hitNum / constants.get(Constants.KEY.NR_REQUEST).intValue());
    }

    private double getAnalyticHitProbability() {
        return constants.get(Constants.KEY.HIT_PROB).doubleValue();
    }

    private void generateServers() {
        //Generate server for this simulation
        double lambda = constants.get(Constants.KEY.REQUEST_RATE).doubleValue();
        cache = new FCFSServer(0, new double[]{lambda * (2 - constants.get(Constants.KEY.HIT_PROB).doubleValue())}, new double[]{constants.get(Constants.KEY.SERVICE_RATE).doubleValue()}, Constants.DIST.EXPO, this);
        download = new ISServer(1, new double[]{(1 - constants.get(Constants.KEY.HIT_PROB).doubleValue()) * lambda}, new double[]{constants.get(Constants.KEY.DOWNLOAD_RATE).doubleValue()}, Constants.DIST.CONST, this);
        servers = new ArrayList<>();
        servers.add(cache);
        servers.add(download);
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
            RequestEvent tmp = new RequestEvent(0, Constants.EVENT_TYPE.ARRIVAL, Constants.MESSAGE.REQUEST, time);
            addEvent(tmp);
            eventNum++;
        }
    }

    // Return the server based ont Id
    private Server getServer(int i) {
        return servers.get(i);
    }

    /**
     * executes the simulation
     */
    private void startSimulation() {
        generateEvents();
        RequestEvent event;
        SimulationLogging.getLogger().info("----------------Start Simulation-------------------");

        while (hasMoreEvents()) {
            event = (RequestEvent) pollNextEvent();
            //SimulationLogging.getLogger().info(event.toString());
            assert event.getTriggerTime() >= clock;
            clock = event.getTriggerTime();

            switch (event.getType()) {

                case ARRIVAL:
                    if (eventNum < constants.get(Constants.KEY.NR_REQUEST).intValue()) {
                        double time = RandomGen.exp(constants.get(Constants.KEY.REQUEST_RATE).doubleValue());
                        RequestEvent tmp = new RequestEvent(0, Constants.EVENT_TYPE.ARRIVAL, Constants.MESSAGE.REQUEST, time + clock);
                        addEvent(tmp);
                        eventNum++;
                    }
                    int cId = event.getCurrentCache();
                    assert cId == cache.getId();
                    cache.add(event, clock);
                    break;
                case DEPARTURE:
                    cId = event.getCurrentCache();
                    if (cId == cache.getId()) {
                        cache.departure(event, clock);
                        if (event.getMessageType() == Constants.MESSAGE.REQUEST) {
                            boolean hit = RandomGen.uniform() < constants.get(Constants.KEY.HIT_PROB).doubleValue();
                            if (hit) {
                                hitNum++;
                            } else {
                                event.setCurrentCache(download.getId());
                                event.setBirthTime(clock);
                                download.add(event, clock);
                            }
                        } else if (event.getMessageType() == Constants.MESSAGE.DATA) {
                            //finishedTasks++;
                        }
                    } else if (cId == download.getId()) {
                        download.departure(event, clock);
                        event.setCurrentCache(cache.getId());
                        event.setMessageType(Constants.MESSAGE.DATA);
                        event.setBirthTime(clock);
                        cache.add(event, clock);
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
