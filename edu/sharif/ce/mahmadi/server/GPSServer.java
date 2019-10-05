package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GPSServer extends Server {

    private int NR_FLOWS;
    private double[] flowWeights;
    private Map<Integer, Vector<Event>> processingEvents;
    private int[] nrOfProcessingJobs;
    private double sumOfActiveWeights;




    public GPSServer(int flowNum, int id, double[] arrivalRate, double[] serviceRate, Constants.DIST service_dist, Simulation simulation) {
        super(id, arrivalRate, serviceRate, service_dist, simulation);

        // creating queue for each class
        NR_FLOWS = flowNum;
        flowWeights = new double[flowNum];
        processingEvents = new HashMap<Integer, Vector<Event>>();
        nrOfProcessingJobs = new int[NR_Classes];
        sumOfActiveWeights = 0;
        for (int f = 0; f < NR_FLOWS; f++) {
            processingEvents.put(f + 1, new Vector<Event>());
            flowWeights[f] = f == 0 ? 1 : 2;
        }
    }

    @Override
    public boolean isEmpty() {
        for (int f = 0; f < NR_Classes; f++) {
            if(nrOfProcessingJobs[f]!=0) return false;
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
        double[] oldMiuCoeefficient = new double[NR_FLOWS];
        for (int f = 0; f < NR_FLOWS; f++) oldMiuCoeefficient[f] = getMiuCoefficient(f + 1);
        //put the departure event of the new added event
        double duration = generateServiceTime(newEvent);
        utilization[newEventId - 1].addValue(duration);
        departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + (duration * (1 / oldMiuCoeefficient[flowId - 1])), duration);
        //SimulationLogging.getLogger().finest("Duration: [" + duration + ":" + oldMiuCoeefficient[flowId - 1] + "]");
        if(!isActive(flowId)) sumOfActiveWeights+= flowWeights[flowId-1];
        assert sumOfActiveWeights<=3;
        processingEvents.get(flowId).add(departure);
        nrOfProcessingJobs[newEventId-1]++;
        simulationInstance.addEvent(departure);


        // If the server is idle ...
        if (!isBusy()) {
            setBusy(true);

        } else {
            //update the trigger time of other events
            for (int f = 0; f < NR_FLOWS; f++) {
                double newMiuCoeficient = getMiuCoefficient(f + 1);
                if(newMiuCoeficient!= oldMiuCoeefficient[f]) {
                    for (int i = 0; i < processingEvents.get(f + 1).size(); i++) {
                        double remainTime = (processingEvents.get(f + 1).get(i).getTriggerTime() - time) * (oldMiuCoeefficient[f] / newMiuCoeficient);
                        //SimulationLogging.getLogger().finest("Remain Time: [" + processingEvents.get(f + 1).get(i).getTriggerTime() + "]: [" + time + "]: [" + processingEvents.get(f + 1).size() + "]: [" + oldMiuCoeefficient[f] + "]: [" + newMiuCoeficient + "]");
                        processingEvents.get(f + 1).get(i).setTriggerTime(time + remainTime);
                        //SimulationLogging.getLogger().finest("Trigger Time: [" + processingEvents.get(f + 1).get(i).getTriggerTime() + "]");
                        simulationInstance.updateExistingEvent(processingEvents.get(f + 1).get(i));
                    }
                }
            }
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

//    private double sumOfActiveWeights() {
//        int sum = 0;
//        for (int f = 0; f < NR_FLOWS; f++) {
//            if (!processingEvents.get(f + 1).isEmpty()) sum += flowWeights[f];
//        }
//        return sum;
//    }

    private boolean isActive(int flowId){
        return !processingEvents.get(flowId).isEmpty();
    }

    private int nrOfProcessingJobs(int classId) {
        return nrOfProcessingJobs[classId-1];
    }

    @Override
    public boolean departure(Event oldEvent, double time) {

        int flowId = ((RequestEvent) oldEvent).getFlowId();

        // Update the time value
        if (totalTime < time) totalTime = time;

        // The departure event should be the processing event
        if (processingEvents.get(flowId).contains(oldEvent)) {

            int oldEventId = oldEvent.getClassId();
            responseTime[oldEventId - 1].addValue(time - oldEvent.getBirthTime());
            queueLength[oldEventId - 1].addSizeValue(time, nrOfProcessingJobs(oldEventId));

            //compute old miu share for each class
            double[] oldMiuCoefficient = new double[NR_FLOWS];
            for (int f = 0; f < NR_FLOWS; f++) oldMiuCoefficient[f] = getMiuCoefficient(f + 1);

            processingEvents.get(flowId).remove(oldEvent);
            nrOfProcessingJobs[oldEventId-1]--;
            if(!isActive(flowId))  sumOfActiveWeights-= flowWeights[flowId-1];
            assert sumOfActiveWeights>=0 && sumOfActiveWeights<=3;

            //SimulationLogging.getLogger().fine("Response time: [" + oldEvent.getId() + "]: [" + time + "]: [" + oldEvent.getBirthTime() + "]: [" + responseTime[oldEvent.getClassId() - 1].sum() + "]: [" + (time - oldEvent.getBirthTime()) + "]");

            // Peek the next event!
            if (!isEmpty()) {
                //update the trigger time of other events
                for (int f = 0; f < NR_FLOWS; f++) {
                    double newMiuCoeficient = getMiuCoefficient(f + 1);
                    if(newMiuCoeficient != oldMiuCoefficient[f]) {
                        for (int i = 0; i < processingEvents.get(f + 1).size(); i++) {
                            double remainTime = (processingEvents.get(f + 1).get(i).getTriggerTime() - time) * oldMiuCoefficient[f] / newMiuCoeficient;
                            //SimulationLogging.getLogger().finest("Remain Time: [" + processingEvents.get(f + 1).get(i).getTriggerTime() + "]: [" + time + "]: [" + processingEvents.get(f + 1).size() + "]: [" + oldMiuCoefficient[f] + "]: [" + newMiuCoeficient + "]");
                            processingEvents.get(f + 1).get(i).setTriggerTime(time + remainTime);
                            //SimulationLogging.getLogger().finest("Trigger Time: [" + processingEvents.get(f + 1).get(i).getTriggerTime() + "]");
                            simulationInstance.updateExistingEvent(processingEvents.get(f + 1).get(i));
                        }
                    }
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
}
