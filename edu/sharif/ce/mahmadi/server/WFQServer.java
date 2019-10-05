package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.*;

public class WFQServer extends Server {

    private int NR_FLOWS;
    private double[] flowWeights;
    private Map<Integer, LinkedList<Event>> processingEvents;
    private List<Double> flowFinishTags;
    private List<Double> headerFinishTags;

    private int[] nrOfProcessingJobs;
    private double sumOfActiveWeights;
    private double virtualTime;
    private double prev_time;


    public WFQServer(int flowNum, int id, double[] arrivalRate, double[] serviceRate, Constants.DIST service_dist, Simulation simulation) {
        super(id, arrivalRate, serviceRate, service_dist, simulation);

        // creating queue for each class
        NR_FLOWS = flowNum;
        flowWeights = new double[flowNum];
        processingEvents = new HashMap<>();
        flowFinishTags = new ArrayList<>();
        headerFinishTags = new ArrayList<>();
        for (int f = 0; f < NR_FLOWS; f++) {
            processingEvents.put(f + 1, new LinkedList<Event>());
            flowFinishTags.add(f, (double) Double.MAX_VALUE);
            headerFinishTags.add(f, (double) 0);
            flowWeights[f] = f == 0 ? 1 : 2;
        }
        sumOfActiveWeights = 0;
        virtualTime = 0;
        prev_time = 0;
        nrOfProcessingJobs = new int[NR_Classes];

    }

    @Override
    public boolean isEmpty() {
        for (int f = 0; f < NR_Classes; f++) {
            if (nrOfProcessingJobs[f] != 0) return false;
        }
        return true;
    }

    @Override
    public double computeAnalyticWaitingTime(int flowId) {
        return 0;
    }

    @Override
    public double getServiceTime(int flowId) {
        return getResponseTime(flowId).getMean();
    }

    @Override
    public boolean add(Event newEvent, double time) {

        Event departure = null;


        // Update the arrival value
        int newEventId = newEvent.getClassId();
        registerRequest(time, newEvent.getClassId());

        // update queueLength statistic
        queueLength[newEventId - 1].addSizeValue(time, nrOfProcessingJobs(newEventId));
        waitingTime[newEventId - 1].addValue(0);

        int flowId = ((RequestEvent) newEvent).getFlowId();
        double duration = generateServiceTime(newEvent);
        utilization[newEventId - 1].addValue(duration);
        //SimulationLogging.getLogger().finest("Duration: [" + duration + ":" + oldMiuCoeefficient[flowId - 1] + "]");
        if (time > prev_time) {
            virtualTime += sumOfActiveWeights != 0 ? (time - prev_time) / sumOfActiveWeights : 0;
            prev_time = time;
        }

        double newFinishTag = Math.max(headerFinishTags.get(flowId - 1), virtualTime) + duration / flowWeights[flowId - 1];
        headerFinishTags.set(flowId - 1, newFinishTag);

        if (!isActive(flowId)) {
            sumOfActiveWeights += flowWeights[flowId - 1];
            assert sumOfActiveWeights <= 3;
            flowFinishTags.add(flowId - 1, newFinishTag);
        } else {
            flowFinishTags.set(flowId - 1, flowFinishTags.get(flowId - 1) + (duration / flowWeights[flowId - 1]));
        }

        departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration, duration);
        processingEvents.get(flowId).addLast(departure);
        nrOfProcessingJobs[newEventId - 1]++;

        // If the server is idle ...
        if (!isBusy()) {
            setBusy(true);
            simulationInstance.addEvent(departure);
        }
        return true;
    }

    private double getMiuCoefficient(int flowId) {
        //double sumActive = sumOfActiveWeights();
        if (isActive(flowId) && sumOfActiveWeights != 0)
            return (flowWeights[flowId - 1] / (processingEvents.get(flowId).size() * sumOfActiveWeights));
        else if (!isActive(flowId) && sumOfActiveWeights != 0)
            return (flowWeights[flowId - 1] / (sumOfActiveWeights + flowWeights[flowId - 1]));
        else if (sumOfActiveWeights == 0) return 1;
        return 0;
    }


    private boolean isActive(int flowId) {
        return !processingEvents.get(flowId).isEmpty();
    }

    private int nrOfProcessingJobs(int classId) {
        return nrOfProcessingJobs[classId - 1];
    }

    @Override
    public boolean departure(Event oldEvent, double time) {

        int flowId = ((RequestEvent) oldEvent).getFlowId();

        // Update the time value
        if (totalTime < time) totalTime = time;

        // The departure event should be the processing event
        if (processingEvents.get(flowId).peek().equals(oldEvent)) {

            int oldEventId = oldEvent.getClassId();
            responseTime[oldEventId - 1].addValue(time - oldEvent.getBirthTime());
            queueLength[oldEventId - 1].addSizeValue(time, nrOfProcessingJobs(oldEventId));

            if (time > prev_time) {
                virtualTime += sumOfActiveWeights != 0 ? (time - prev_time) / sumOfActiveWeights : 0;
                prev_time = time;
            }

            processingEvents.get(flowId).poll();
            nrOfProcessingJobs[oldEventId - 1]--;
            if (!isActive(flowId)) {
                sumOfActiveWeights -= flowWeights[flowId - 1];
                assert sumOfActiveWeights >= 0 && sumOfActiveWeights <= 3;
            } else {
                headerFinishTags.set(flowId - 1, headerFinishTags.get(flowId - 1) + processingEvents.get(flowId).peek().getDuration() / flowWeights[flowId - 1]);
            }

            // Peek the next event!
            if (!isEmpty()) {
                double min = Double.MAX_VALUE;
                int minId = -1;
                for (int f = 0; f < NR_FLOWS; f++) {
                    if (isActive(f + 1) && min > headerFinishTags.get(f)) {
                        min = headerFinishTags.get(f);
                        minId = f + 1;
                    }
                }
                if (minId != -1) {
                    processingEvents.get(minId).peek().setTriggerTime(time +  processingEvents.get(minId).peek().getDuration());
                    simulationInstance.addEvent( processingEvents.get(minId).peek());
                } else assert false;
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


//    private double sumOfActiveWeights() {
//        int sum = 0;
//        for (int f = 0; f < NR_FLOWS; f++) {
//            if (!processingEvents.get(f + 1).isEmpty()) sum += flowWeights[f];
//        }
//        return sum;
//    }
}
