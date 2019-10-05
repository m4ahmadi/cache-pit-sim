package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Constants.DIST;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.LinkedList;
import java.util.Queue;

public class FCFSServer extends Server {


    private Queue<Event> queue = null; //stores the events and scheduling
    private Event processingEvent;
    private int[] nrOfWaitingJobs;


    public FCFSServer(int id, double[] requestRate, double[] serviceRate, DIST service_dist, Simulation simulation) {
        super(id, requestRate, serviceRate, service_dist, simulation);
        queue = new LinkedList<Event>();
        nrOfWaitingJobs =  new int[NR_Classes];
        processingEvent = null;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public double getAverageSystemLength(int flowId) {
        if (flowId == 0) {
            double sum = 0;
            for (int i = 1; i <= NR_Classes; i++) {
                sum += queueLength[i - 1].realSum();
            }
            return sum / totalTime + this.getUtilization(flowId);
        }
        return (queueLength[flowId - 1].realSum() / totalTime) + this.getUtilization(flowId);
    }

    @Override
    public boolean add(Event newEvent, double time) {

        // Update the arrival value
        int newEventId = newEvent.getClassId();
        registerRequest(time, newEventId);

        // If the server is idle ...
        if (!isBusy()) {
            //collecting statistic
            setBusy(true);
            waitingTime[newEventId - 1].addValue(0);
            //generate service time and departure event
            double duration = generateServiceTime(newEvent);
            utilization[newEventId-1].addValue(duration);
            Event departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration, duration);
            processingEvent = departure;
            simulationInstance.addEvent(departure);

            return true;
        } else {
            //assumes inifinite capacity
            queueLength[newEventId - 1].addSizeValue(time, nrOfWatingJobs(newEvent.getClassId()));
            nrOfWaitingJobs[newEventId-1]++;
            return queue.add(newEvent);

        }
    }

    private int nrOfWatingJobs(int classId) {
//        int num = 0;
//        for (Event event : queue) {
//            if (event.getClassId() == classId) num++;
//        }
        return nrOfWaitingJobs[classId-1];
    }

    @Override
    public boolean departure(Event oldEvent, double time) {
        // Update the time value
        if (totalTime < time) totalTime = time;

        // The departure event should be the processing event
        if (processingEvent.equals(oldEvent)) {
            int oldEventId = oldEvent.getClassId();

            responseTime[oldEventId - 1].addValue(time - oldEvent.getBirthTime());

            // Peek the next event!
            if (!isEmpty()) {
                Event newEvent = queue.peek();
                int newEventId = newEvent.getClassId();
                queueLength[newEvent.getClassId() - 1].addSizeValue(time, nrOfWatingJobs(newEventId));
                queue.poll();
                nrOfWaitingJobs[newEventId-1]--;
                waitingTime[newEventId - 1].addValue(time - newEvent.getBirthTime());
                double duration = generateServiceTime(newEvent);
                utilization[newEventId-1].addValue(duration);
                Event departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration, duration);
                processingEvent = departure;
                simulationInstance.addEvent(departure);
            } else {
                setBusy(false);
                processingEvent = null;
            }
            return true;
        } else {
            SimulationLogging.getLogger().severe("Processing event is not equal to the departure event");
            assert false;
            return false;
        }
    }
}
