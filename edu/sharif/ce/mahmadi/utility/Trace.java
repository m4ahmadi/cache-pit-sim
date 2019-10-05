package edu.sharif.ce.mahmadi.utility;

import edu.sharif.ce.mahmadi.caches.AnalyticMultiClassMahdiehONOFFLRUCache;
import edu.sharif.ce.mahmadi.caching.RequestEvent;
import edu.sharif.ce.mahmadi.simulation.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Trace {

    private File traceFile;
    private double trimProb;
    private Scanner scanner;
    public static double startTime = 0;
    Map<Integer, Integer> nrOfRequests = null;
    Map<Integer, List<Double>> timeStamps = null;
    Map<Integer, List<Integer>> classId = null;


    public static double endTime = Long.MAX_VALUE;


    private Set<Integer> contents;

    public Trace(File mtraceFile, double mtrimProb) throws FileNotFoundException {
        this.traceFile = mtraceFile;
        scanner = new Scanner(traceFile);
        nrOfRequests = new HashMap<>();
        timeStamps = new HashMap<>();
        classId = new HashMap<>();
        trimProb = mtrimProb;
    }

    public RequestEvent readTrace() {

        RequestEvent request = null;
        try {

            while (request == null) {
                String[] lineSplit = scanner.nextLine().trim().split("\t");
                int fId = Integer.parseInt(lineSplit[0]);
                double interRequestTime = Double.parseDouble(lineSplit[1]);


                if (startTime > startTime + interRequestTime) {
                    assert false;
                }
                double triggerTime = startTime + interRequestTime;
                startTime += interRequestTime;

                if (nrOfRequests.containsKey(fId)) {
                    if (RandomGen.uniform() >= trimProb) {
                        nrOfRequests.put(fId, nrOfRequests.get(fId).intValue() + 1);
                        timeStamps.get(fId).add(triggerTime);

                        request = new RequestEvent(fId, 1, Constants.MESSAGE.REQUEST, triggerTime);
                    } else {
                        request = null;
                    }
                } else {
                    nrOfRequests.put(fId, 1);
                    timeStamps.put(fId, new LinkedList<Double>());
                    timeStamps.get(fId).add(triggerTime);
                    request = new RequestEvent(fId, 1, Constants.MESSAGE.REQUEST, triggerTime);
                }
            }
            assert request != null;
            return request;

        } catch (NoSuchElementException e) {
            System.out.println("Exception thrown: " + e);
            return null;
        }
    }


    public double getAverageRequestDensity() {
        int sumOfRequests = 0;
        for (Integer fId : nrOfRequests.keySet()) {
            sumOfRequests += nrOfRequests.get(fId).intValue();
        }
        return ((double) sumOfRequests / nrOfRequests.keySet().size());
    }

    public int getNrOfContents() {
        return nrOfRequests.size();
    }

    public void calculateEffectiveLifeTime() {

        for (int id = 1; id <= 6; id++) {
            classId.put( id, new LinkedList<Integer>());
        }
        for (Integer fId : nrOfRequests.keySet()) {
            int nrOfRequestsPerFId = nrOfRequests.get(fId).intValue();
            if (nrOfRequestsPerFId < 10) {
                classId.get(6).add(fId);
                continue;
            }
            double time10 = timeStamps.get(fId).get((int) (0.1 * nrOfRequestsPerFId) - 1);
            double time90 = timeStamps.get(fId).get((int) (0.9 * nrOfRequestsPerFId) - 1);
            double effectiveLifeTime = (time90 - time10) / 86400.0;
            if (effectiveLifeTime <= 2) {
                classId.get(1).add(fId);
            } else if (effectiveLifeTime > 2 && effectiveLifeTime <= 5) {
                classId.get(2).add(fId);
            } else if (effectiveLifeTime > 5 && effectiveLifeTime <= 8) {
                classId.get(3).add(fId);
            } else if (effectiveLifeTime > 8 && effectiveLifeTime <= 13) {
                classId.get(4).add(fId);
            } else if (effectiveLifeTime > 13) {
                classId.get(5).add(fId);
            }
        }
    }

    public void printStatistics() {
        try {
            FileWriter stats = new FileWriter("trace/statistcis.txt");
            FileWriter vmFile = new FileWriter("trace/vm.txt");

            StringBuilder st = new StringBuilder();
            StringBuilder vm = new StringBuilder();

            for (int id = 1; id <= 6; id++) {
                System.out.println("classId = " + id);
                for(Integer fId : classId.get(id)){
                    System.out.println("fId = " + id);
                    vmFile.write(fId + "\t" + id + "\t" + nrOfRequests.get(fId).intValue()+  "\n");
                }
                st.append(id + "\t" + classId.get(id).size() + "\n");
            }
            stats.write(st.toString());
            //vm.toString());
            stats.flush();
            vmFile.flush();
            stats.close();
            vmFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
