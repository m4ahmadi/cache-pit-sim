package edu.sharif.ce.mahmadi.caches;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Constants.POLICY;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.Dist;
import edu.sharif.ce.mahmadi.utility.Statistic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicCache {

    // index variables
    protected double[] nr_request_index;
    protected double[] nr_hit_index;
    protected double[] nr_pit_index;

    //total variables
    protected double nr_requests;
    protected double nr_hit;
    protected double nr_pit;
    protected double nr_forward;
    protected double responseTime;

    protected int catalogSize;
    protected Dist popularityDist; // popularity distribution
    protected int Id;
    double theoreticalDownTime;

    protected Simulation simulationInstance;
    protected AnalyticCache analyticCache;
    private Map<Integer, List<RequestEvent>> pendingRequests; //PIT
    private boolean PIT;
    private boolean INDEX;


    public BasicCache(int id, boolean pit, boolean index, double downloadRate, Simulation simulation, Dist mpopularityDist) {

        resetStatsitcis();
        popularityDist = mpopularityDist;
        Id = id;
        PIT = pit;
        catalogSize = popularityDist.getCatalogSize();
        theoreticalDownTime = downloadRate != 0.0 ? (1 / downloadRate) : 0.0;
        simulationInstance = simulation;
        INDEX = false;
        if (simulation.getSimulationMode() == true) {
            INDEX = index;
            if (PIT) {
                pendingRequests = new HashMap<Integer, List<RequestEvent>>();
                this.PIT = pit;
            }
            if (INDEX) {
                nr_request_index = new double[catalogSize];
                nr_hit_index = new double[catalogSize];
                nr_pit_index = new double[catalogSize];
            }
        }
    }

    public void resetStatsitcis(){
        nr_requests = 0;
        nr_forward = 0;
        nr_pit = 0;
        nr_hit = 0;
        responseTime =0.0;

    }


    protected double getTheoreticalArrivalRate(int fId) {
        return popularityDist.getArrivalRate(fId);
    }

    public double getTheoreticalArrivalRate() {
        return popularityDist.getArrivalRate();
    }


    public double computeAnalyticHitProbability(int fId) {
        return analyticCache.computeAnalyticHitProbability(fId);
    }

    ;

    public double computeAnalyticForwardingRate(int fId) {
        return analyticCache.computeAnalyticForwardingRate(fId);
    }

    public double computeAnalyticPitHitProbability(int fId) {
        return analyticCache.computeAnalyticPitHitProbability(fId);
    }

    public double computeAnalyticResponseTime(int fId) {
        return analyticCache.computeAnalyticResponseTime(fId);
    }

    public double getCTime() {
        return analyticCache.getCTime();
    }

    public double getHitProbability(int fId) {
        if (INDEX) {
            return (nr_request_index[fId - 1] != 0) ? (nr_hit_index[fId - 1] / nr_request_index[fId - 1]) : 0;
        } else {
            return 0;
        }
    }

    public double getHitRate(int fId) {
        if (INDEX) {
            return (nr_hit_index[fId - 1] / simulationInstance.getTotalSimulationTime());
        } else {
            return 0;
        }
    }

    public double getPitHitProbability(int fId) {
        if (INDEX) {
            return (nr_request_index[fId - 1] != 0) ? (nr_pit_index[fId - 1] / nr_request_index[fId - 1]) : 0;
        } else {
            return 0;
        }
    }

    public double getPitHitRate(int fId) {
        if (INDEX) {
            return (nr_pit_index[fId - 1] / simulationInstance.getTotalSimulationTime());
        } else {
            return 0;
        }
    }

    public double getForwardingProbability(int fId) {
        if (INDEX) {
            return ((nr_request_index[fId - 1] != 0) ? (1 - getHitProbability(fId) - getPitHitProbability(fId)) : 0);
        } else return 0;
    }

    public double getForwardingRate(int fId) {
        if (INDEX) {
            return (nr_request_index[fId - 1] - nr_pit_index[fId - 1] - nr_hit_index[fId - 1]) / simulationInstance.getTotalSimulationTime();
        } else {
            return 0;
        }
    }

    public double getArrivalRate(int fId) {
        if (INDEX) {
            return nr_request_index[fId - 1] / simulationInstance.getTotalSimulationTime();
        } else return 0;
    }

    public double getHitProbability() {
        return (nr_requests != 0) ? (nr_hit / nr_requests) : 0;
    }

    public double getPitHitProbability() {
        return (nr_requests != 0) ? (nr_pit / nr_requests) : 0;
    }

    public double getForwardingProbability() {
        return (nr_requests != 0) ? (1 - getHitProbability() - getPitHitProbability()) : 0;
    }

    public double getArrivalRate() {
        return simulationInstance.getTotalSimulationTime() != 0 ? nr_requests / simulationInstance.getTotalSimulationTime() : 0;
    }

    public double getHitRate() {
        return (simulationInstance.getTotalSimulationTime() != 0) ? ((nr_hit) / simulationInstance.getTotalSimulationTime()) : 0;
    }

    public double getPITHitRate() {
        return (simulationInstance.getTotalSimulationTime() != 0) ? (nr_pit / simulationInstance.getTotalSimulationTime()) : 0;
    }

    public double getForwardingRate() {
        return (simulationInstance.getTotalSimulationTime() != 0) ? ((nr_requests - nr_hit - nr_pit) / simulationInstance.getTotalSimulationTime()) : 0;
    }

    public double getForwardingNumber() {
        return (nr_requests - nr_pit - nr_hit);
    }

    public double getResponseTime() {
        return (responseTime/nr_requests);
    }

    public double computeAnalyticResponseTime() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticResponseTime(f + 1));
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

    public double computeAnalyticPitHitProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticPitHitProbability(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticForwardingProbability() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(popularityDist.getPDF(f + 1) * computeAnalyticForwardingProbability(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticHitRate() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticHitRate(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticPitHitRate() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticPitHitRate(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticForwardingRate() {
        Statistic mean = new Statistic(false);
        for (int f = 0; f < catalogSize; f++) {
            mean.addValue(computeAnalyticForwardingRate(f + 1));
        }
        return mean.realSum();
    }

    public double computeAnalyticForwardingNumber() {
        return computeAnalyticForwardingRate() * simulationInstance.getTotalSimulationTime();
    }

    public double computeAnalyticForwardingProbability(int fId) {
        return computeAnalyticForwardingRate(fId) / (getTheoreticalArrivalRate(fId));
    }

    public double computeAnalyticHitRate(int fId) {
        return computeAnalyticHitProbability(fId) * (getTheoreticalArrivalRate(fId));
    }

    public double computeAnalyticPitHitRate(int fId) {
        return computeAnalyticPitHitProbability(fId) * (getTheoreticalArrivalRate(fId));
    }


    public double getTheoreticalDownTime() {
        return theoreticalDownTime;
    }

    public Dist getPopularityDist() {
        return popularityDist;
    }

    public void receiveMessage(RequestEvent request) {
        processMessage(request);
    }

    public void processMessage(RequestEvent request) {

        int fId = request.getfId();

        if (request.getMessageType() == Constants.MESSAGE.DATA) {
            //SimulationLogging.getLogger().info(String.format("%s\t%f\t%s\t", "Write!!", time, request.toString()));
            if (PIT) {
                for (RequestEvent e : pendingRequests.get(fId)) {
                    responseTime += (simulationInstance.getTotalSimulationTime() - e.getStartTime());
                 }
                //pitPerFileLength[fId - 1].addSizeValue(time, nrOfPendingRequests(fId) == 0 ? 0 : 1);
                //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                pendingRequests.remove(fId);
            }
            if (!PIT) {
                //responseTime[fId - 1].addValue(time - request.getStartTime());
            }
            writeMessageInTheCache(fId);
            return;
        } else if (request.getMessageType() == Constants.MESSAGE.REQUEST) {
            if (INDEX) nr_request_index[fId - 1]++;
            nr_requests++;
            boolean hit = isInTheCache(fId);
            if (hit) {
                //SimulationLogging.getLogger().info(String.format("%s\t%s\t", "HIT!!", request.toString()));
                if (INDEX) nr_hit_index[fId - 1]++;
                nr_hit++;
                //responseTime[fId - 1].addValue(time - request.getStartTime());
            } else {

                boolean hasPendingRequest = hasPendingRequest(request);
                if (!hasPendingRequest || !PIT) {
                    //SimulationLogging.getLogger().info(String.format("%s\t%s\t", "MISS!!", request.toString()));
                    //request.setDownloadStartTime(time);
                    if (PIT) {
                        //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                        //pitPerFileLength[fId - 1].addSizeValue(time, hasPendingRequest ? 1 : 0);
                        if (!pendingRequests.containsKey(fId)) pendingRequests.put(fId, new ArrayList<RequestEvent>());
                        pendingRequests.get(fId).add(request);
                    }
                    //cycleLength[fId - 1].addEndValue(request.getStartTime());
                    nr_forward++;
                    simulationInstance.download(request, theoreticalDownTime);

                } else if (hasPendingRequest) {
                    //SimulationLogging.getLogger().info("PIT!!" + request.toString());
                    if (INDEX) nr_pit_index[fId - 1]++;
                    nr_pit++;
                    //pitLength.addSizeValue(time, nrOfPendingFileRequests(), false);
                    //pitPerFileLength[fId - 1].addSizeValue(time, hasPendingRequest ? 1 : 0);
                    pendingRequests.get(fId).add(request);
                }
            }
        }
    }

    private boolean hasPendingRequest(RequestEvent request) {
        if (!pendingRequests.containsKey(request.getfId()) || pendingRequests.get(request.getfId()).isEmpty()) {
            return false;
        }
        return true;
    }

    protected abstract void writeMessageInTheCache(int fId);

    protected abstract boolean isInTheCache(int fId);

    public abstract void evictContent(int fId);

    public void printCatalogIndexStatistics(String fileName) {
        try {
            FileWriter[] stats = new FileWriter[3];
            stats[0] = new FileWriter(fileName + "-cshit-sim.txt");
            stats[1] = new FileWriter(fileName + "-pithit-sim.txt");
            stats[2] = new FileWriter(fileName + "-forward-sim.txt");

            StringBuilder[] st = new StringBuilder[3];
            st[0] = new StringBuilder();
            st[1] = new StringBuilder();
            st[2] = new StringBuilder();

            //st.append("Prob\tRate\tAHit\tSHit\tCI\tAW\tSW\tCI\tACL\tSCL\tCI\tAPIT\tSPIT\tCI\n");
            for (int f = 0; f < catalogSize; f++) {
                double rate = popularityDist.getArrivalRate(f + 1);
                st[0].append((f + 1) + "\t" + computeAnalyticHitProbability(f + 1) + "\t" + getHitProbability(f + 1) + "\t" + computeAnalyticHitRate(f + 1) + "\t" + getHitRate(f + 1) + "\n");
                st[1].append((f + 1) + "\t" + computeAnalyticPitHitProbability(f + 1) + "\t" + getPitHitProbability(f + 1) + "\t" + computeAnalyticPitHitRate(f + 1) + "\t" + getPitHitRate(f + 1) + "\n");
                st[2].append((f + 1) + "\t" + computeAnalyticForwardingProbability(f + 1) + "\t" + getForwardingProbability(f + 1) + "\t" + computeAnalyticForwardingRate(f + 1) + "\t" + getForwardingRate(f + 1) + "\n");

            }
            for (int i = 0; i < 3; i++) {
                stats[i].write(st[i].toString());
                stats[i].flush();
                stats[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printClassStatistics(String fileName) {
        try {
            FileWriter stats = new FileWriter(fileName + "-cshit-sim.txt");
            StringBuilder st = new StringBuilder();
            Constants.POLICY policy = Constants.POLICY.values()[simulationInstance.getConstant().get(Constants.KEY.POLICY).intValue()];

            for (int classId = 1; classId <=6 ; classId++) {
                //st.append((f + 1) + "\t" + computeAnalyticHitProbability(f + 1) + "\t" + getHitProbability(f + 1) + "\t" + computeAnalyticHitRate(f + 1) + "\t" + getHitRate(f + 1) + "\n");
                if(policy ==  POLICY.LRUMultiClassONOFFMahdieh) {
                    st.append(classId + "\t" + ((AnalyticMultiClassMahdiehONOFFLRUCache) analyticCache).computeAnalyticPerClassHitProbability(classId) + "\t" + ((AnalyticMultiClassMahdiehONOFFLRUCache) analyticCache).computeAnalyticPerClassPDF(classId) + "\n");
                }else  if(policy ==  POLICY.TwoLRUMultiClassONOFFMahdieh) {
                    st.append(classId + "\t" + ((AnalyticMultiClassMahdiehONOFFTwoLRUCache) analyticCache).computeAnalyticPerClassHitProbability(classId) + "\t" + ((AnalyticMultiClassMahdiehONOFFTwoLRUCache) analyticCache).computeAnalyticPerClassPDF(classId) + "\n");
                }

            }
                stats.write(st.toString());
                stats.flush();
                stats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
