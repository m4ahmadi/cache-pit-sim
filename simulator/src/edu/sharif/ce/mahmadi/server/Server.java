package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.simulation.Constants.DIST;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.RandomGen;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;
import edu.sharif.ce.mahmadi.utility.Statistic;

import java.util.LinkedList;
import java.util.List;

public abstract class Server {

    // Simulation statistics
    protected Statistic[] waitingTime;
    protected Statistic[] responseTime;
    protected Statistic[] queueLength;
    protected Statistic[] utilization;
    protected Statistic[] serviceRate;
    protected double[] arrivalSum;
    protected double totalTime;
    protected double totalArrivalTime;
    protected int NR_Classes;
    Statistic interRequestTimeStamps;
    List<Double> requestTimeStamps;
    //simulation instance
    Simulation simulationInstance;
    //Analytical
    protected double[] miu;
    private int Id;
    private boolean busy;
    private double[] lambda;
    DIST service_dist;


    public Server(int id, double[] arrivalRate, double[] serviceRate, DIST service_dist, Simulation simulation) {
        Id = id;
        NR_Classes = arrivalRate.length;
        this.miu = new double[NR_Classes];
        this.lambda = new double[NR_Classes];
        this.arrivalSum = new double[NR_Classes];
        this.serviceRate = new Statistic[NR_Classes];
        this.waitingTime = new Statistic[NR_Classes];
        this.responseTime = new Statistic[NR_Classes];
        this.queueLength = new Statistic[NR_Classes];
        this.utilization = new Statistic[NR_Classes];

        for (int i = 0; i < NR_Classes; i++) {
            this.miu[i] = serviceRate[i];
            this.lambda[i] = arrivalRate[i];
            arrivalSum[i] = 0;
            this.serviceRate[i] = new Statistic(false);
            waitingTime[i] = new Statistic(false);
            responseTime[i] = new Statistic(false);
            queueLength[i] = new Statistic(false);
            utilization[i] = new Statistic(false);
        }

        this.service_dist = service_dist;
        simulationInstance = simulation;
        interRequestTimeStamps = new Statistic(false);
        requestTimeStamps = new LinkedList<>();
        requestTimeStamps.add(0.0);
        totalTime = 0;
        totalArrivalTime = 0;
    }

    public int getId() {
        return Id;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getTotalArrivalTime() {
        return totalArrivalTime;
    }

    public double getLambda(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 0; i < NR_Classes; i++) {
                sum += lambda[i];
            }
            return sum;
        }
        return lambda[flowId - 1];
    }

    public double getMiu(int flowId) {
        return miu[flowId - 1];
    }

    public Statistic getWaitingTime(int flowId) {
        if (flowId == 0) {
            Statistic sum = new Statistic(false);
            for (int i = 0; i < NR_Classes; i++) {
                sum.addReplication(waitingTime[i]);
            }
            return sum;
        }
        //SimulationLogging.getLogger().severe(String.valueOf(waitingTime[flowId - 1].getSize()));
        return waitingTime[flowId - 1];
    }

    public Statistic getResponseTime(int flowId) {
        if (flowId == 0) {
            Statistic sum = new Statistic(false);
            for (int i = 0; i < NR_Classes; i++) {
                sum.addReplication(responseTime[i]);
            }
            return sum;
        }
        //SimulationLogging.getLogger().severe(String.valueOf(responseTime[flowId - 1].getSize()) + responseTime[flowId - 1].sum());
        return responseTime[flowId - 1];
    }

    public double getAverageSystemLength(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += queueLength[i - 1].realSum();
            }
            return sum / totalArrivalTime;
        }
        return queueLength[flowId - 1].realSum() / totalArrivalTime;
    }

    public double getUtilization(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += getUtilization(i);
            }
            return sum;
        }
        //SimulationLogging.getLogger().severe(String.valueOf(utilization[flowId - 1].sum()) + "\t" + totalArrivalTime);
        return (utilization[flowId - 1].realSum() / totalArrivalTime);
    }

    public double getArrivalRate(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += getArrivalRate(i);
            }
            return sum;
        }
        //SimulationLogging.getLogger().severe(String.valueOf(arrivalSum[flowId - 1]));
        if(Id==3){return totalArrivalTime!=0 ? (arrivalSum[flowId - 1] / totalArrivalTime) : 0;}
        return flowId == 1 ? getLambda(flowId) :  (totalArrivalTime!=0) ? (arrivalSum[flowId - 1] / totalArrivalTime) : 0;
    }

    public Statistic getServiceRate(int flowId) {
        return serviceRate[flowId - 1];
    }

    public Statistic getInterRequestTimeStamps() {
        return interRequestTimeStamps;
    }

    public double computeAnalyticWaitingTime(int flowId) {
        return (getArrivalRate(flowId)!=0) ? computeAnalyticQueueLength(flowId) / getArrivalRate(flowId) : 0;
    }

    public double computeAnalyticResponseTime(int flowId) {
        //SimulationLogging.getLogger().severe(flowId + ": " + computeAnalyticSystemLength(flowId) + ": " + getArrivalRate(flowId));
        double t_util = computeAnalyticUtilization(0);
        if (flowId == 0) {
            return computeAnalyticSystemLength(flowId) / getArrivalRate(flowId);
        }
        if (service_dist.equals(DIST.EXPO)) {
            return (t_util) / ((1 - t_util)*miu[flowId-1]);
        } else if (service_dist.equals(DIST.CONST)) {
            return (t_util * 0.5) / ((1 - t_util)*miu[flowId-1]);
        }
        return 0;
    }

    public double computeAnalyticSystemLength(int flowId) {
        double util = computeAnalyticUtilization(flowId);
        return computeAnalyticQueueLength(flowId) + util;
    }

    public double computeAnalyticQueueLength(int flowId) {
        /*if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += computeAnalyticQueueLength(i);
            }
            return sum;
        }*/
        double util = computeAnalyticUtilization(flowId);
        double t_util = computeAnalyticUtilization(0);

        if (service_dist.equals(DIST.EXPO)) {
            return ((util * t_util) / (1 - t_util));
        } else if (service_dist.equals(DIST.CONST)) {
            return ((t_util * util * 0.5) / (1 - t_util));
        }
        return 0;
    }

    public double computeAnalyticUtilization(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += computeAnalyticUtilization(i);
            }
            return sum;
        }
        double util = (miu[flowId - 1] != 0) ? (getArrivalRate(flowId)/   miu[flowId - 1]  /*getServiceRate(flowId).getMean()*/) : 0;
        return util;
    }

    public boolean isBusy() {
        return busy;
    }

    protected void setBusy(boolean state) {
        busy = state;
    }

    private void setDist(DIST dist) {
        service_dist = dist;
    }

    protected void registerRequest(double time, int flowId) {
        arrivalSum[flowId - 1]++;
        //interRequestTimeStamps.addValue(time - requestTimeStamps.get(requestTimeStamps.size() - 1), false);
        //requestTimeStamps.add(time);
        if (totalTime < time) totalTime = time;
        if (totalArrivalTime < time) totalArrivalTime = time;

    }

    protected double generateServiceTime(Event event) {
        double duration = 0;
        int classId = event.getClassId();
        if (service_dist.equals(DIST.EXPO) && miu[classId - 1] != 0) duration = RandomGen.exp(miu[classId - 1]);
        else if (service_dist.equals(DIST.CONST) || miu[classId - 1] == 0) {
            duration = (miu[classId - 1] != 0) ? (1 / miu[classId - 1]) : 0;
        } else {
            SimulationLogging.getLogger().severe("INVALID Distribution for service time!");
        }
        serviceRate[classId - 1].addValue(duration);
        return duration;
    }

    public double computeAnalyticalServiceTime(int flowId) {
        return (miu[flowId - 1] != 0) ? (1 / miu[flowId - 1]) : 0;
    }

    //if the queue is empty or not
    public abstract boolean isEmpty();

    public abstract boolean add(Event newEvent, double time);

    public abstract boolean departure(Event oldEvent, double time);

    public double getServiceTime(int flowId) {
        return serviceRate[flowId - 1].getMean() != 0 ? serviceRate[flowId - 1].getMean() : 0;
    }
}
