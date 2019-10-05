package edu.sharif.ce.mahmadi.simulation;

import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.server.Event;
import edu.sharif.ce.mahmadi.simulation.Constants.KEY;
import edu.sharif.ce.mahmadi.simulation.Constants.REF;
import edu.sharif.ce.mahmadi.utility.SimulationLogging;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.logging.Level;


public class Simulation {

    protected double clock;
    protected int iteration;
    protected int eventNum;

    protected PriorityQueue<Event> eventList;
    protected EnumMap<KEY, Number> constants;


    public Simulation(Constants.REF ref) {
        clock = 0; // time is zero
        iteration = 0;
        eventNum = 0;
        this.constants = Constants.CONSTS.get(ref);
        this.eventList = new PriorityQueue<Event>();
        SimulationLogging.getLogger().log(Level.SEVERE, constants.toString());
    }

    public static void main(String[] argv) {

        if (argv.length >= 2) readArguments(argv[0], argv[1]);
        if (argv.length >= 3) SimulationLogging.getLogger().setLevel(Level.parse(argv[2]));

        if (argv[0].equals(Constants.REF.DEHGHAN.toString())) {
            //dehghanSimulation();
            SimulationLogging.getLogger().severe("NO Simulation of this type!!");
        } else if (argv[0].equals(Constants.REF.NETWORKTwoLevel.toString())) {
            //networkSimulation();
            SimulationLogging.getLogger().severe("NO Simulation of this type!!");
        } else if (argv[0].equals(Constants.REF.TTLCache.toString())) {
            //TTLCacheSimulation();
            SimulationLogging.getLogger().severe("NO Simulation of this type!!");
        } else if (argv[0].equals(REF.Queue.toString())) {
            SimulationLogging.getLogger().severe("Queue Simulation!");
            QueueSimulation.RUN();
        } else if (argv[0].equals(REF.QueueNetwork.toString())) {
            SimulationLogging.getLogger().severe("Queue Network Simulation!");
            QueueNetworkSimulation.RUN();
        } else if (argv[0].equals(REF.CacheNetwork.toString())) {
            SimulationLogging.getLogger().severe("Cache Network Simulation!");
            CacheNetworkSimulation.RUN();
        } else if (argv[0].equals(REF.BasicCacheNetwork.toString())) {
            SimulationLogging.getLogger().severe("Basic Cache Network Simulation!");
            BasicCacheNetworkSimulation.RUN(argv[3]);
        } else {
            SimulationLogging.getLogger().severe("NO Simulation of this type!!");
        }

    }

    private static void readArguments(String ref, String inputFile) {
        try {
            Scanner input = new Scanner(new File(inputFile));
            while (input.hasNextLine()) {
                String newLine = input.nextLine();
                String split[] = newLine.split("=");
                SimulationLogging.getLogger().info(String.format("%s\t%s\n", split[0], split[1]));
                Constants.CONSTS.get(REF.valueOf(ref)).put(Constants.KEY.valueOf(split[0]), Double.valueOf(split[1]));
            }
            input.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public EnumMap<KEY, Number> getConstant() {
        return this.constants;
    }

    public boolean hasMoreEvents() {
        return eventList != null && !eventList.isEmpty();
    }

    public Event peekNextEvent() {
        return eventList.peek();
    }

    public Event pollNextEvent() {
        return eventList.poll();
    }

    public void addEvent(Event event) {
        //SimulationLogging.getLogger().info(event.toString());
        eventList.add(event);
    }

    public void removeCorrespondingEvent(Event event) {
        assert eventList.remove(event);
    }

    public void updateExistingEvent(Event event) {
        //SimulationLogging.getLogger().finest(event.toString());
        if (eventList.remove(event)) {
            eventList.add(event);
        } else {
            SimulationLogging.getLogger().severe("NO Event in the eventqueue!!");
            assert 0 == 1;
        }
    }

    public void requestFromOthers(RequestEvent event) {
    }

    public void download(RequestEvent event, double downloadTime) {
    }

    public double getTotalSimulationTime() {
        return clock;
    }
    public void resetSimulationTime() {
        clock = 0;
        eventNum = 0;
    }

    public boolean getSimulationMode() {
        return constants.get(KEY.SIMULATION_MODE).intValue() >= 1;
    }

}
