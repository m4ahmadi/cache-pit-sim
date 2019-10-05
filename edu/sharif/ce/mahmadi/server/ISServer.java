package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.simulation.Constants;
import edu.sharif.ce.mahmadi.simulation.Simulation;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.util.Vector;

public class ISServer extends Server {


    //private Vector<Event> processingEvents = null; //stores the events in parallel
    private int[] nrOfProcessingJobs;


    public ISServer(int id, double[] requestRate, double[] meanServiceTime, Constants.DIST service_dist, Simulation simulation) {
        super(id, requestRate, meanServiceTime, service_dist, simulation);
        //processingEvents = new Vector<Event>();
        nrOfProcessingJobs = new int[NR_Classes];
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean add(Event newEvent, double time) {

        Event departure;

        // Update the arrival value
        int newEventId = newEvent.getClassId();
        registerRequest(time, newEventId);

        // update queueLength statistic
        //queueLength[newEventId - 1].addSizeValue(time, nrOfProcessingJobs(newEventId));

        // If the server is idle ...
        if (!isBusy()) {
            setBusy(true);
        }
        //generate service time and departure event
        double duration = generateServiceTime(newEvent);
        utilization[newEventId-1].addValue(duration);
        departure = newEvent.update(Constants.EVENT_TYPE.DEPARTURE, time + duration, duration);
        //processingEvents.add(departure);
        nrOfProcessingJobs[newEventId-1]++;
        simulationInstance.addEvent(departure);
        return true;

    }

    @Override
    public boolean departure(Event oldEvent, double time) {
        // Update the time value
        if (totalTime < time) totalTime = time;

        // The departure event should be the processing event
       // if (processingEvents.contains(oldEvent)) {
            int oldEventId = oldEvent.getClassId();
            responseTime[oldEventId - 1].addValue(time - oldEvent.getBirthTime());
            queueLength[oldEventId - 1].addSizeValue(time, nrOfProcessingJobs(oldEventId));
         //   processingEvents.remove(oldEvent);
            nrOfProcessingJobs[oldEventId-1]--;
            //SimulationLogging.getLogger().fine("Response time: [" + oldEvent.getId() + "]: [" + time + "]: [" + oldEvent.getBirthTime() + "]: [" + responseTime[oldEvent.getClassId() - 1].sum() + "]: [" + (time - oldEvent.getBirthTime()) + "]");

            // Peek the next event!
            //if (processingEvents.isEmpty()) {
            //    setBusy(false);
            //}
            return true;
        //} else {
          //  SimulationLogging.getLogger().severe("Processing event is not equal to the departure event");
           // assert false;
            //return false;
        //}
    }

    private int nrOfProcessingJobs(int classId) {
        /*int num = 0;
        for(Event event: processingEvents){
            if(event.getClassId()==classId)
                num++;
        }*/
        return nrOfProcessingJobs[classId-1];
    }

    @Override
    public double computeAnalyticWaitingTime(int flowId) {
        return 0;
    }

    @Override
    public double computeAnalyticQueueLength(int flowId) {
        return 0;
    }
}
