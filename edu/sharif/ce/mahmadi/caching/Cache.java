package edu.sharif.ce.mahmadi.caching;


import edu.sharif.ce.mahmadi.server.*;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Statistic;
import edu.sharif.ce.mahmadi.utility.Zipf;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Cache {

    protected double[] nr_request;
    protected double[] nr_hit;
    protected double[] nr_pit;


    protected int catalogSize;
    protected Dist popularityDist; // popularity distribution

    protected Simulation simulationInstance;
    protected Constants.SCHEDULING serverScheduling;
    //protected Statistic pitLength;
    //protected Statistic cacheLength;
    //output Values
    protected double[] theoreticalHit;
    protected double[] theoreticalResponseTime;
    protected double[] theoreticalDownloadRate;
    protected double[] theoreticalPitHitProb;
    //private Statistic[] responseTime;
    //private Statistic[] cycleLength;
    private double theoreticalDownTime;
    private Map<Integer, List<RequestEvent>> pendingRequests; //PIT
    private Server server;
    private boolean PIT;


    public Cache(Constants.SCHEDULING scheduling, boolean pit, int id, double[] arrivalRate, double[] serviceRate, double downloadRate, Constants.DIST service_dist, Simulation simulation, double zipf, int mcatalogSize) {

        serverScheduling = scheduling;
        popularityDist = new Zipf(zipf, mcatalogSize, true);
        //popularityDist = new Uniform(mcatalogSize);

        switch (scheduling) {
            case FCFS:
                server = new FCFSServer(id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            case PS:
                server = new PSServer(id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            case GPS:
                server = new GPSServer(1, id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            case WFQ:
                server = new WFQServer(1, id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            case PGPS:
                server = new PGPSServer(1, new double[]{1000.0, 1.000}, id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            case IS:
                server = new ISServer(id, arrivalRate, serviceRate, service_dist, simulation);
                break;
            default:
                break;
        }

        theoreticalDownTime = downloadRate != 0 ? (1 / downloadRate) : 0;
        simulationInstance = simulation;
        pendingRequests = new HashMap<Integer, List<RequestEvent>>();
        this.PIT = pit;
        //The number of requests and hit for each file
        catalogSize = mcatalogSize;
        nr_request = new double[mcatalogSize];
        nr_hit = new double[mcatalogSize];
        nr_pit = new double[mcatalogSize];

        theoreticalHit = new double[catalogSize];
        theoreticalResponseTime = new double[catalogSize];
        theoreticalDownloadRate = new double[catalogSize];
        theoreticalPitHitProb = new double[catalogSize];

        //Delay Time
        //this.responseTime = new Statistic[catalogSize];
        //this.cycleLength = new Statistic[catalogSize];
        //this.pitLength = new Statistic(false);
        //this.pitPerFileLength = new Statistic[catalogSize];
        //this.cacheLength = new Statistic(false);
        //for (int f = 0; f < catalogSize; f++) {
            //this.downloadTime[f] = new Statistic(false);
            //responseTime[f] = new Statistic((id == 1 && simulationInstance.getConstant().get(Constants.KEY.PRINT_INDEX).intValue() == 1));
            //cycleLength[f] = new Statistic(false);
            //pitPerFileLength[f] = new Statistic();
        //}

    }


    public abstract double computeAnalyticHitProbability(int fId);

    public abstract double computeAnalyticResponseTime(int fId);

    public abstract double computeAnalyticDownloadRate(int fId);

    public abstract double computeAnalyticPitProbability(int fId);

    public abstract double getCTime();


    public double getHitProbability(int fId) {
        return (nr_request[fId - 1] != 0) ? (nr_hit[fId - 1] / nr_request[fId - 1]) : 0;
    }

    public double getHitProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * getHitProbability(f + 1));
        }
        //SimulationLogging.getLogger().severe(Double.toString(mean.sum()));
        return mean.realSum();
    }

    public double getArrivalRate(int flowId) {
        return server.getArrivalRate(flowId);
    }

    public Statistic getServiceRate(int flowId) {
        return server.getServiceRate(flowId);
    }

    //public double getDownloadTime(int fId) {
        //return downloadTime[fId - 1].getMean();
    //}

//    public double getDownloadTime() {
//        Statistic mean = new Statistic(false);
//        for (int f = 0; f < catalogSize; f++) {
//            mean.addReplication(downloadTime[f]);
//        }
//        //SimulationLogging.getLogger().severe(Double.toString(mean.getSize()));
//        return mean.getMean();
//    }

    public double getResponseTime(int fId) {
        //return responseTime[fId - 1].getMean();
        return 0;
    }

    public double getResponseTime() {
        //Statistic mean = new Statistic();
        Statistic wmean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            wmean.addValue(popularityDist.getPDF(f + 1) * getResponseTime(f + 1));
            //mean.addReplication(responseTime[f]);
        }
        //SimulationLogging.getLogger().severe("tr" + String.valueOf(mean.getSize()));
        return wmean.realSum();
    }

//    public Statistic getResponseTimeStatistic() {
//        Statistic sum = new Statistic(true);
//        for (int f = 0; f < catalogSize; f++) {
//            sum.addReplication(responseTime[f]);
//        }
//        return sum;
//    }

//    public Statistic getResponseTimeStatistic(int fId) {
//        return responseTime[fId - 1];
//    }

//    public Statistic getCycleLength(int fId) {
//        //SimulationLogging.getLogger().severe("cycle: " + cycleLength[fId - 1].getMean(server.getTotalTime()) + "\t" + String.valueOf(cycleLength[fId - 1].getSize()));
//        return cycleLength[fId - 1];
//    }

    //public double getAverageCacheLength() {
    //    return cacheLength.realSum() / server.getTotalTime();
    //}

//    public Statistic getCacheLengthStatistic() {
//        return cacheLength;
//    }

    public double getPitHitProbability(int fId) {
        return (nr_request[fId - 1] != 0) ? (nr_pit[fId - 1] / nr_request[fId - 1]) : 0;
    }

    public double getPitHitProbability() {
        Statistic wmean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            wmean.addValue(popularityDist.getPDF(f + 1) * getPitHitProbability(f + 1));
        }
        return wmean.realSum();
    }

    public double getForwardingRate(int fId) {
        return (nr_request[fId - 1] - nr_pit[fId - 1] - nr_hit[fId - 1]) / server.getTotalTime();
    }

    public double getForwardingProbability(int fId) {
        return ((nr_request[fId - 1] != 0) ? ((nr_request[fId - 1] - nr_pit[fId - 1] - nr_hit[fId - 1]) / nr_request[fId - 1]) : 0);
    }

    public double getForwardingRate() {
        Statistic wmean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            wmean.addValue(getForwardingRate(f + 1));
        }
        return wmean.realSum();
    }

    public double getForwardingProbability() {
        Statistic wmean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            wmean.addValue(popularityDist.getPDF(f + 1) * getForwardingProbability(f + 1));
        }
        return wmean.realSum();
    }

    public double getForwardingNumber() {
        Statistic wmean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            wmean.addValue(nr_request[f] - nr_pit[f] - nr_hit[f]);
        }
        return wmean.realSum();
    }
    //public double getAveragePitLength(int fId) {
    //return pitPerFileLength[fId - 1].sum() / server.getTotalTime();
    //}

    //public double getAveragePitLength() {
    //    return pitLength.realSum() / server.getTotalTime();
    //}

    //public Statistic getPitLengthStatistic() {
    //    return pitLength;
    //}

    public Statistic getServerWaitingTime(int flowId) {
        return server.getWaitingTime(flowId);
    }

    public Statistic getServerResponseTime(int flowId) {
        return server.getResponseTime(flowId);
    }

    public double getServerServiceTime(int flowId) {
        return server.getServiceTime(flowId);
    }

    public double getAverageSystemLength(int flowId) {
        return server.getAverageSystemLength(flowId);
    }

    public Statistic getServerInterRequestTimeStamps() {
        return server.getInterRequestTimeStamps();
    }

    public double getTotalTime() {
        return server.getTotalTime();
    }

    public double getTotalArrivalTime() {
        return server.getTotalArrivalTime();
    }

    public double getUtilization(int flowId) {
        return server.getUtilization(flowId);
    }

    public double getLambda(int flowId) {
        return server.getLambda(flowId);
    }

    public double getMiu(int flowId) {
        return server.getMiu(flowId);
    }

    /*public double computeAnalyticDownloadRate() {
        Statistic sum = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            sum.addValue(computeAnalyticDownloadRate(f + 1));
        }
        return sum.realSum();
    }*/

    public double computeAnalyticResponseTime() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticResponseTime(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticPitHitProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticPitProbability(f + 1));
        }
        return mean.realSum();
    }

     public double computeAnalyticForwardingRate() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticDownloadRate(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticForwardingProbability(int fId) {
        return computeAnalyticDownloadRate(fId)/ (popularityDist.getPDF(fId) * getLambda(1));
    }

    public double computeAnalyticForwardingProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticForwardingProbability(f+1));
        }
        return mean.realSum();
    }

    public double computeAnalyticForwardingNumber() {
        return computeAnalyticForwardingRate() * server.getTotalTime() ;
    }

    public double computeAnalyticExpectedPITSize() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticPitProbability(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticExpectedCacheSize() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticHitProbability(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticHitProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticHitProbability(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticServerServiceTime(int flowId) {
        return server.computeAnalyticalServiceTime(flowId);
    }

    public double getTheoreticalDownTime() {
        return theoreticalDownTime;
    }

    public double computeAnalyticServerWaitingTime(int flowId) {
        return server.computeAnalyticWaitingTime(flowId);
    }

    public double computeAnalyticServerResponseTime(int flowId) {
        return server.computeAnalyticResponseTime(flowId);
    }

    public double computeAnalyticUtilization(int flowId) {
        return server.computeAnalyticUtilization(flowId);
    }

    public double computeAnalyticSystemLength(int flowId) {
        return server.computeAnalyticSystemLength(flowId);
    }

    public int getId() {
        return server.getId();
    }

    public void receiveMessage(RequestEvent request, double time) {

        if (request.getMessageType() == Constants.MESSAGE.DATA) {
            //downloadTime[request.getfId() - 1].addValue(time - request.getDownloadStartTime());
        }
        server.add(request, time);
    }

    public void processMessage(RequestEvent request, double time) {

        int fId = request.getfId();


        if (request.getMessageType() == Constants.MESSAGE.WRITE) {
            //SimulationLogging.getLogger().info(String.format("%s\t%f\t%s\t", "Write!!", time, request.toString()));
            if (PIT) {
                ArrayList<RequestEvent> tmp = new ArrayList<>();
                for (RequestEvent e : pendingRequests.get(fId)) {
                    //responseTime[fId - 1].addValue(time - e.getStartTime());
                    tmp.add(e);
                }
                //pitPerFileLength[fId - 1].addSizeValue(time, nrOfPendingRequests(fId) == 0 ? 0 : 1);
                //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                pendingRequests.remove(fId);
            }
            if (!PIT) {
                //responseTime[fId - 1].addValue(time - request.getStartTime());
            }
            writeMessageInTheCache(fId, time);
            return;
        }

        server.departure(request, time);

        if (request.getMessageType() == Constants.MESSAGE.REQUEST) {
            nr_request[fId - 1]++;
            boolean hit = isInTheCache(fId, time);
            if (hit) {
                //SimulationLogging.getLogger().info(String.format("%s\t%s\t", "HIT!!", request.toString()));
                nr_hit[fId - 1]++;
                //responseTime[fId - 1].addValue(time - request.getStartTime());

            } else {

                boolean hasPendingRequest = hasPendingRequest(request);
                if (!hasPendingRequest || !PIT) {
                    //SimulationLogging.getLogger().info(String.format("%s\t%s\t", "MISS!!", request.toString()));
                    request.setDownloadStartTime(time);
                    if (PIT) {
                        //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                        //pitPerFileLength[fId - 1].addSizeValue(time, hasPendingRequest ? 1 : 0);
                        if (!pendingRequests.containsKey(fId)) pendingRequests.put(fId, new ArrayList<RequestEvent>());
                        pendingRequests.get(fId).add(request);
                    }
                    //cycleLength[fId - 1].addEndValue(request.getStartTime());
                    simulationInstance.requestFromOthers(request);

                } else if (hasPendingRequest) {
                    //SimulationLogging.getLogger().info("PIT!!" + request.toString());
                    nr_pit[fId - 1]++;
                    //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                    //pitPerFileLength[fId - 1].addSizeValue(time, hasPendingRequest ? 1 : 0);
                    pendingRequests.get(fId).add(request);
                }
            }
        } else if (request.getMessageType() == Constants.MESSAGE.DATA) {


        } else {
            assert false;
        }
    }

    private int nrOfPendingRequests(int fId) {
        int size = pendingRequests.containsKey(fId) ? pendingRequests.get(fId).size() : 0;
        return ((size > 0) ? size : 0);
    }

    private int nrOfPendingFileRequests() {
        int num = pendingRequests.size();
//        for (int key : pendingRequests.keySet()) {
////            if (pendingRequests.get(key).size() != 0) num++;
////        }
        return num;
    }

    private boolean hasPendingRequest(RequestEvent request) {
        if (!pendingRequests.containsKey(request.getfId()) || pendingRequests.get(request.getfId()).isEmpty()) {
            return false;
        }
        return true;
    }

    protected abstract void writeMessageInTheCache(int fId, double time);

    protected abstract boolean isInTheCache(int fId, double time);

    public Dist getPopularityDist() {
        return popularityDist;
    }

    public abstract void evictContent(int i, double time);

    public void formatCatalogHitProbability(String fileName) {
        try {
            Statistic responseTimeFlow[] = new Statistic[2];
            FileWriter stats = new FileWriter(fileName + "-index.txt");
            StringBuilder st = new StringBuilder();
            st.append("Prob\tRate\tAHit\tSHit\tCI\tAW\tSW\tCI\tACL\tSCL\tCI\tAPIT\tSPIT\tCI\n");
            for (int f = 0; f < catalogSize; f++) {
//                if (responseTimeFlow[((f < 800) ? 2 : 1) - 1] == null)
//                    responseTimeFlow[((f < 800) ? 2 : 1) - 1] = new Statistic(true);
//                else responseTimeFlow[((f < 800) ? 2 : 1) - 1].addReplication(getResponseTimeStatistic(f + 1));
                st.append((f + 1) + "\t" + popularityDist.getPDF(f + 1) + "\t" + popularityDist.getPDF(f + 1) * getLambda(1) +
                        "\t" + theoreticalHit[f] + "\t" + getHitProbability(f + 1) + "\t" + theoreticalResponseTime[f] +
                        "\t" + getResponseTime(f + 1) + "\t" + 0 + "\t" + computeAnalyticDownloadRate(f+1) +
                        "\t" + getForwardingRate(f+1) + "\t" +computeAnalyticPitProbability(f+1) + "\t" + getPitHitProbability(f + 1) +
                        "\t" + computeAnalyticForwardingProbability(f+1) + "\t" + getForwardingProbability(f+1) + "\n");
            }
//            for (int f = 0; f < 2; f++) {
//                st.append((f + 1) + "\t" + responseTimeFlow[f].getMean() + "\t" + responseTimeFlow[f].getPercentile(75) + "\t" + responseTimeFlow[f].getPercentile(50) + "\t" + responseTimeFlow[f].getMax() + "\n");
//            }
            stats.write(st.toString());
            stats.flush();
            stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
