package edu.sharif.ce.mahmadi.simulation;

import edu.sharif.ce.mahmadi.server.*;
import edu.sharif.ce.mahmadi.utility.RandomGen;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;
import edu.sharif.ce.mahmadi.utility.Statistic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class QueueSimulation extends Simulation {

    private ArrayList<Server> servers; // array of caches
    private int eventNum;

    public QueueSimulation(Constants.REF ref) {
        super(ref);
        clock = 0; // time is zero
        eventNum = 0;
        servers = new ArrayList<Server>();
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
        // printMiuServerPlot(basePath);
        // printMiuCachePlot(basePath);
    }

    private static void singleExecution(String basePath) {

        QueueSimulation replication = null;
        Statistic serviceRate = new Statistic(true);
        Statistic arrivalRate = new Statistic(true);
        Statistic waitingTime = new Statistic(true);
        Statistic responseTime = new Statistic(true);
        Statistic systemLength = new Statistic(true);
        Statistic utilization = new Statistic(true);


        try {
            String fileName = "[" + Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.SCHEDULING).intValue() + "][" + Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.DIST).intValue() + "]";
            FileWriter stats = new FileWriter(basePath + fileName + "stats.txt");
            int iterationNum = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.ITERATION_NUM).intValue();
            for (int i = 0; i < iterationNum; i++) {
                replication = new QueueSimulation(Constants.REF.Queue);
                replication.startSimulation();
                arrivalRate.addValue(replication.servers.get(0).getArrivalRate(1));
                serviceRate.addReplication(replication.servers.get(0).getServiceRate(1));
                waitingTime.addReplication(replication.servers.get(0).getWaitingTime(1));
                responseTime.addReplication(replication.servers.get(0).getResponseTime(1));
                systemLength.addValue(replication.servers.get(0).getAverageSystemLength(1));
                utilization.addValue(replication.servers.get(0).getUtilization(1));
            }

            //arrival
            double analytic = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.REQUEST_RATE).doubleValue();
            double simulation = arrivalRate.getMean();
            double absoluteError = Math.abs(analytic - simulation);
            double relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, arrivalRate.getConfidenceInterval(), absoluteError, relError));

            //serviceRate
            analytic = Constants.CONSTS.get(Constants.REF.Queue).get(Constants.KEY.SERVICE_RATE).doubleValue();
            simulation = 1 / serviceRate.getMean();
            absoluteError = Math.abs(analytic - simulation);
            relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, serviceRate.getConfidenceInterval(), absoluteError, relError));

            //waiting time
            analytic = replication.servers.get(0).computeAnalyticWaitingTime(1);
            simulation = waitingTime.getMean();
            absoluteError = Math.abs(analytic - simulation);
            relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, waitingTime.getConfidenceInterval(), absoluteError, relError));

            //response time
            analytic = replication.servers.get(0).computeAnalyticResponseTime(1);
            simulation = responseTime.getMean();
            absoluteError = Math.abs(analytic - simulation);
            relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, responseTime.getConfidenceInterval(), absoluteError, relError));

            //utilization
            analytic = replication.servers.get(0).computeAnalyticUtilization(1);
            simulation = utilization.getMean();
            absoluteError = Math.abs(analytic - simulation);
            relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, utilization.getConfidenceInterval(), absoluteError, relError));

            //system length
            analytic = replication.servers.get(0).computeAnalyticSystemLength(1);
            simulation = systemLength.getMean();
            absoluteError = Math.abs(analytic - simulation);
            relError = absoluteError / analytic;
            stats.write(String.format("%s\t%s\t%s\t%s\t%s\n", analytic, simulation, systemLength.getConfidenceInterval(), absoluteError, relError));

            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateServers() {
        if (constants.get(Constants.KEY.SCHEDULING).intValue() == Constants.SCHEDULING.FCFS.ordinal()) {
            servers.add(0, new FCFSServer(1, new double[]{constants.get(Constants.KEY.REQUEST_RATE).doubleValue()}, new double[]{constants.get(Constants.KEY.SERVICE_RATE).doubleValue()}, Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()], this));

        } else if (constants.get(Constants.KEY.SCHEDULING).intValue() == Constants.SCHEDULING.PS.ordinal()) {
            servers.add(0, new PSServer(1, new double[]{constants.get(Constants.KEY.REQUEST_RATE).doubleValue()}, new double[]{constants.get(Constants.KEY.SERVICE_RATE).doubleValue()}, Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()], this));

        } else if (constants.get(Constants.KEY.SCHEDULING).intValue() == Constants.SCHEDULING.IS.ordinal()) {
            servers.add(0, new ISServer(1, new double[]{constants.get(Constants.KEY.REQUEST_RATE).doubleValue()}, new double[]{constants.get(Constants.KEY.SERVICE_RATE).doubleValue()}, Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()], this));
        } else if (constants.get(Constants.KEY.SCHEDULING).intValue() == Constants.SCHEDULING.GPS.ordinal()) {
            servers.add(0, new GPSServer(2, 1, new double[]{constants.get(Constants.KEY.REQUEST_RATE).doubleValue()}, new double[]{constants.get(Constants.KEY.SERVICE_RATE).doubleValue()}, Constants.DIST.values()[constants.get(Constants.KEY.DIST).intValue()], this));
        }
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
            Event tmp = new Event(Constants.EVENT_TYPE.ARRIVAL, 1, time, time);
            addEvent(tmp);
            eventNum++;
        }
    }

    /**
     * executes the simulation
     */
    protected void startSimulation() {
        generateEvents();
        Event event = null;
        SimulationLogging.getLogger().info("----------------Start Simulation-------------------");

        while (hasMoreEvents()) {
            event = pollNextEvent();
            //SimulationLogging.getLogger().info(event.toString());
            assert event.getTriggerTime() >= clock;
            clock = event.getTriggerTime();

            switch (event.getType()) {

                case ARRIVAL:
                    if (eventNum < constants.get(Constants.KEY.NR_REQUEST).intValue()) {
                        double time = RandomGen.exp(constants.get(Constants.KEY.REQUEST_RATE).doubleValue());
                        Event tmp = new Event(Constants.EVENT_TYPE.ARRIVAL, eventNum%2+1, time + clock, time + clock);
                        addEvent(tmp);
                        eventNum++;
                    }
                    servers.get(0).add(event, clock);
                    break;
                case DEPARTURE:
                    servers.get(0).departure(event, clock);
                    break;
                default:
                    SimulationLogging.getLogger().info("NO SUCH EVENT!");
                    break;
            }
        }
    }
}
