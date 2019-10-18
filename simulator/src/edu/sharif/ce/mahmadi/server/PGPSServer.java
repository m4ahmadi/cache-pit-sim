package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PGPSServer extends Server {

    private int NR_FLOWS;
    private double[] flowWeights;
    private Map<Integer, LinkedList<Event>> processingEvents;
    private int[] nrOfProcessingJobs;
    private double sumOfActiveWeights;


    public PGPSServer(int flowNum, double[] m_flowWeights, int id, double[] arrivalRate, double[] serviceRate, Constants.DIST service_dist, Simulation simulation) {
        super(id, arrivalRate, serviceRate, service_dist, simulation);

        // creating queue for each class
        NR_FLOWS = flowNum;
        flowWeights = new double[flowNum];
        processingEvents = new HashMap<Integer, LinkedList<Event>>();
        nrOfProcessingJobs = new int[NR_Classes];
        sumOfActiveWeights = 0;

        for (int f = 0; f < NR_FLOWS; f++) {
            processingEvents.put(f + 1, new LinkedList<Event>());
            flowWeights[f] = m_flowWeights[f];
        }
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
        // Update the arrival value
        int newEventId = newEvent.getClassId();
        registerRequest(time, newEventId);
        // update queueLength statistic
        queueLength[newEventId - 1].addSizeValue(time, nrOfProcessingJobs(newEventId));
        nrOfProcessingJobs[newEventId - 1]++;

        int flowId = ((RequestEvent) newEvent).getFlowId();
        double[] oldMiuCoeefficient = new double[NR_FLOWS];

        if (!isActive(flowId)) {
            // If the server is idle ...
            double oldSumOfActiveWeights = sumOfActiveWeights;
            sumOfActiveWeights += flowWeights[flowId - 1];
            //assert sumOfActiveWeights <= 3;

            waitingTime[newEventId - 1].addValue(0);
            //generate service time and departure event
            double duration = generateServiceTime(newEvent);
            utilization[newEventId - 1].addValue(duration);
            Event departure = null;


            if (!isBusy()) {
                setBusy(true);
                departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration, duration);

            } else {
                departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration / getMiuCoefficient(flowId), duration);
                for (int f = 0; f < NR_FLOWS; f++) {
                    if (f != flowId - 1 && !processingEvents.get(f + 1).isEmpty()) {
                        double remainTime = (processingEvents.get(f + 1).peek().getTriggerTime() - time) * ((sumOfActiveWeights) / oldSumOfActiveWeights);
                        processingEvents.get(f + 1).peek().setTriggerTime(time + remainTime);
                        simulationInstance.updateExistingEvent(processingEvents.get(f + 1).peek());
                    }

                }
            }

            processingEvents.get(flowId).addLast(departure);
            simulationInstance.addEvent(departure);

            return true;
        } else {
            assert isBusy();
            processingEvents.get(flowId).addLast(newEvent);
            return true;
        }

    }

    private double getMiuCoefficient(int flowId) {
        //double sumActive = sumOfActiveWeights();
        if (sumOfActiveWeights != 0) return (flowWeights[flowId - 1] / (sumOfActiveWeights));
        else return 0;
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

            processingEvents.get(flowId).poll();
            nrOfProcessingJobs[oldEventId - 1]--;

            if (!isActive(flowId)) {
                if (!isEmpty()) {
                    //update
                    for (int f = 0; f < NR_FLOWS; f++) {
                        if (f != flowId - 1 &&  !processingEvents.get(f + 1).isEmpty()) {
                            double remainTime = (processingEvents.get(f + 1).peek().getTriggerTime() - time) * ((sumOfActiveWeights - flowWeights[flowId - 1]) / sumOfActiveWeights);
                            processingEvents.get(f + 1).peek().setTriggerTime(time + remainTime);
                            simulationInstance.updateExistingEvent(processingEvents.get(f + 1).peek());
                        }
                    }

                } else {
                    setBusy(false);
                }
                sumOfActiveWeights -= flowWeights[flowId - 1];
                //assert sumOfActiveWeights >= 0 && sumOfActiveWeights <= 3;
            } else {
                //peek next, no update
                Event newEvent = processingEvents.get(flowId).peek();
                int newEventId = newEvent.getClassId();
                waitingTime[newEventId - 1].addValue(time - newEvent.getBirthTime());
                double duration = generateServiceTime(newEvent);
                utilization[newEventId - 1].addValue(duration);
                Event departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration / getMiuCoefficient(flowId), duration);
                assert processingEvents.get(flowId).peek().equals(departure);
                simulationInstance.addEvent(departure);
            }
            return true;
        } else {
            SimulationLogging.getLogger().severe("Processing event is not equal to the departure event");
            assert false;
            return false;
        }
    }
}
