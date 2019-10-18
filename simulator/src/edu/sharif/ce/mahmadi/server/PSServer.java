package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Constants.EVENT_TYPE;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.Vector;

public class PSServer extends Server {

    private Vector<Event> processingEvents = null; //stores the events in parallel
    private int[] nrOfProcessingJobs;


    public PSServer(int id, double[] requestRate, double[] serviceRate, Constants.DIST service_dist, Simulation simulation) {
        super(id, requestRate, serviceRate, service_dist, simulation);
        processingEvents = new Vector<Event>();
        nrOfProcessingJobs = new int[NR_Classes];
    }

    @Override
    public boolean isEmpty() {
        return processingEvents.isEmpty();
    }

    @Override
    public boolean add(Event newEvent, double time) {

        Event departure = null;

        // Update the arrival value
        int newEventId = newEvent.getClassId();
        registerRequest(time, newEventId);

        // update queueLength statistic
        queueLength[newEventId - 1].addSizeValue(time, nrOfProcessingJobs(newEventId));
        waitingTime[newEventId - 1].addValue(0);

        // If the server is idle ...
        if (!isBusy()) {
            setBusy(true);
        } else {
            //update the trigger time of other events
            for (int i = 0; i < processingEvents.size(); i++) {
                double coefficient = (1 + (1 / (double) (processingEvents.size())));
                double remainTime = (processingEvents.get(i).getTriggerTime() - time) * coefficient;
                //SimulationLogging.getLogger().finest("Remain Time: [" + processingEvents.get(i).getTriggerTime() + "]: [" + time + "]: [" + processingEvents.size() + "]");
                processingEvents.get(i).setTriggerTime(time + remainTime);
                //SimulationLogging.getLogger().finest("Trigger Time: [" + processingEvents.get(i).getTriggerTime() + "]");
                simulationInstance.updateExistingEvent(processingEvents.get(i));
            }
        }

        //put the departure event of the new added event
        double duration = generateServiceTime(newEvent);
        utilization[newEventId - 1].addValue(duration);
        departure = newEvent.update(EVENT_TYPE.DEPARTURE, time + duration * (processingEvents.size() + 1), duration);
        processingEvents.add(departure);
        nrOfProcessingJobs[newEventId-1]++;
        simulationInstance.addEvent(departure);
        return true;
    }

    @Override
    public boolean departure(Event oldEvent, double time) {
        // Update the time value
        if (totalTime < time) totalTime = time;

        // The departure event should be the processing event
        if (processingEvents.contains(oldEvent)) {

            int oldEventId = oldEvent.getClassId();
            responseTime[oldEventId - 1].addValue(time - oldEvent.getBirthTime());
            queueLength[oldEventId - 1].addSizeValue(time, nrOfProcessingJobs(oldEventId));
            processingEvents.remove(oldEvent);
            nrOfProcessingJobs[oldEventId-1]--;
            //SimulationLogging.getLogger().fine("Response time: [" + oldEvent.getId() + "]: [" + time + "]: [" + oldEvent.getBirthTime() + "]: [" + responseTime[oldEvent.getClassId() - 1].sum() + "]: [" + (time - oldEvent.getBirthTime()) + "]");

            // Peek the next event!
            if (!processingEvents.isEmpty()) {
                //update the trigger time of other events
                for (int i = 0; i < processingEvents.size(); i++) {
                    double coefficient = (1 - (1 / (double) (processingEvents.size() + 1)));
                    double remainTime = (processingEvents.get(i).getTriggerTime() - time) * coefficient;
                    //SimulationLogging.getLogger().finest("Remain Time: [" + processingEvents.get(i).getTriggerTime() + "]: [" + time + "]: [" + processingEvents.size() + "]");
                    processingEvents.get(i).setTriggerTime(time + remainTime);
                    //SimulationLogging.getLogger().finest("Trigger Time: [" + processingEvents.get(i).getTriggerTime() + "]");
                    simulationInstance.updateExistingEvent(processingEvents.get(i));
                }

            } else {
                setBusy(false);
            }
            return true;
        } else {
            SimulationLogging.getLogger().severe("Processing event is not equal to the departure event");
            assert false;
            return false;
        }
    }

    private int nrOfProcessingJobs(int classId) {
        return nrOfProcessingJobs[classId-1];
    }

    @Override
    public double computeAnalyticWaitingTime(int flowId) {
        return 0;
    }

    @Override
    public double computeAnalyticalServiceTime(int flowId) {
        return computeAnalyticResponseTime(flowId);
    }

    @Override
    public double getServiceTime(int flowId) {
        return getResponseTime(flowId).getMean();
    }

    @Override
    public double computeAnalyticQueueLength(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += computeAnalyticQueueLength(i);
            }
            return sum;
        }
        double util = computeAnalyticUtilization(flowId);
        double t_util = computeAnalyticUtilization(0);
        return ((util * t_util) / (1 - t_util));
    }

    @Override
    public double computeAnalyticResponseTime(int flowId) {
        //SimulationLogging.getLogger().severe(flowId + ": " + computeAnalyticSystemLength(flowId) + ": " + getArrivalRate(flowId));
        double t_util = computeAnalyticUtilization(0);
        if (flowId == 0) {
            return computeAnalyticSystemLength(flowId) / getArrivalRate(flowId);
        }
        if (service_dist.equals(Constants.DIST.EXPO)) {
            return (t_util) / ((1 - t_util)*miu[flowId-1]);
        } else if (service_dist.equals(Constants.DIST.CONST)) {
            return (t_util ) / ((1 - t_util)*miu[flowId-1]);
        }
        return 0;
    }

}
