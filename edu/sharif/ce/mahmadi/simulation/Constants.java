package edu.sharif.ce.mahmadi.simulation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final Map<REF, EnumMap<KEY, Number>> CONSTS = new HashMap<REF, EnumMap<KEY, Number>>();

    static {

        EnumMap<KEY, Number> queue = new EnumMap<>(KEY.class);
        queue.put(KEY.REQUEST_RATE, 50);
        queue.put(KEY.SCHEDULING, SCHEDULING.PS.ordinal());
        queue.put(KEY.DIST, DIST.EXPO.ordinal());
        queue.put(KEY.DEADLINE_DIST, DIST.EXPO.ordinal());
        queue.put(KEY.NR_REQUEST, 10);
        queue.put(KEY.SERVICE_RATE, 52);
        queue.put(KEY.DOWNLOAD_RATE, 52);
        queue.put(KEY.ITERATION_NUM, 1);
        queue.put(KEY.HIT_PROB, 0.5);
        CONSTS.put(REF.Queue, queue);

        queue.put(KEY.ZIPF, 0.8);
        CONSTS.put(REF.QueueNetwork, queue);

        queue.put(KEY.F, 10);
        queue.put(KEY.PIT, 1);
        queue.put(KEY.DATA_SERVICE_RATE, 10);
        queue.put(KEY.POLICY, 0);
        queue.put(KEY.TTL, 55);
        queue.put(KEY.C, 1);
        queue.put(KEY.Z, 1);
        queue.put(KEY.TRACE, 0);
        queue.put(KEY.TRIM, 0.0);
        queue.put(KEY.PRINT_INDEX, 1);

        CONSTS.put(REF.CacheNetwork, queue);
        CONSTS.put(REF.BasicCacheNetwork, queue);



        EnumMap<KEY, Number> dehghan = new EnumMap<>(KEY.class);
        dehghan.put(KEY.F, 30000);
        dehghan.put(KEY.C, 10000);
        dehghan.put(KEY.L, 1);
        dehghan.put(KEY.K, 2);
        dehghan.put(KEY.NR_REQUESTS_PER_CP, 50000);
        CONSTS.put(REF.DEHGHAN, dehghan);

        EnumMap<KEY, Number> network = new EnumMap<>(KEY.class);
        network.put(KEY.TRACE, 0);
        network.put(KEY.F, 10);
        network.put(KEY.C, 5);
        network.put(KEY.L, 3);
        network.put(KEY.K, 0);
        network.put(KEY.MEAN_SERVICE_TIME, 0);
        network.put(KEY.MEAN_DOWNLOAD_TIME, 0.02);
        network.put(KEY.CLUSTER_SIZE, 3);
        network.put(KEY.NR_REQUESTS_PER_LEAF, 100000);
        network.put(KEY.REQUEST_RATE, 10);
        network.put(KEY.COOPERATION, 1);
        network.put(KEY.COUPLING, 1);
        network.put(KEY.CONSTRAINTS, 1);
        network.put(KEY.LINK_DELAY, 0.001);
        network.put(KEY.ZIPF, 0.8);
        network.put(KEY.ZDD, 0);
        network.put(KEY.ITERATION_NUM, 1);
        CONSTS.put(REF.NETWORKTwoLevel, network);

        EnumMap<KEY, Number> ttlCache = new EnumMap<>(KEY.class);
        ttlCache.put(KEY.F, 100);
        ttlCache.put(KEY.C, 100);
        ttlCache.put(KEY.L, 1);
        ttlCache.put(KEY.K, 0);
        ttlCache.put(KEY.MEAN_SERVICE_TIME, 0.005);
        ttlCache.put(KEY.INITIAL_ACCESS_DELAY, 0.005);
        ttlCache.put(KEY.MEAN_DOWNLOAD_TIME, 1);
        ttlCache.put(KEY.CLUSTER_SIZE, 1);
        ttlCache.put(KEY.NR_REQUESTS_PER_LEAF, 100000);
        ttlCache.put(KEY.REQUEST_RATE, 100);
        ttlCache.put(KEY.COOPERATION, 0);
        ttlCache.put(KEY.COUPLING, 0);
        ttlCache.put(KEY.CONSTRAINTS, 0);
        ttlCache.put(KEY.LINK_DELAY, 0);
        ttlCache.put(KEY.ITERATION_NUM, 1);
        ttlCache.put(KEY.ZDD, 0);
        ttlCache.put(KEY.ZIPF, 0.8);
        CONSTS.put(REF.TTLCache, ttlCache);

    }

    // the event type in simulation
    public enum EVENT_TYPE {
        REQUEST, COOPERATION, UPDATE, HIT_REQUEST_END, COOPERATION_END, DATA, MISS_REQUEST_END, SEARCH_END,
        ARRIVAL, DEPARTURE, EVICT,
    }

    // kind of message this packet carries
    public enum MESSAGE {
        REQUEST, DATA, WRITE, EVICT
    }

    //type of simulation
    public enum REF {
        DEHGHAN, NETWORKTwoLevel, TTLCache, Queue, QueueNetwork, CacheNetwork, BasicCacheNetwork
    }

    // kind of scheduling used in queues

    public enum SCHEDULING {
        FCFS, PS, IS, GPS, PGPS, WFQ
    }

    // Caching Policy
    public enum POLICY {
        LRUMahdieh, TwoLRUMahdeih, LRUDehghan, TwoLRUDehghan, LRUONOFFMahdieh, TwoLRUONOFFMahdieh, LRUMultiClassONOFFMahdieh, TwoLRUMultiClassONOFFMahdieh, LFU, TTL, R_TTL, LRU, FIFO
    }

    public enum DIST {
        EXPO, CONST, GENERAL, HYPOEXP
    }

    public enum KEY {

        //common parameters
        REQUEST_RATE, NR_REQUEST, SERVICE_RATE, ITERATION_NUM, TRACE,TRIM,


        //queue simulation parameters
        DIST, SCHEDULING, SIMULATION_MODE,

        // queue network parameters
        DOWNLOAD_RATE, HIT_PROB, PIT, DATA_SERVICE_RATE, POLICY, DEADLINE_DIST, FLOW_NUM,

        //TTL
        TTL,

        //output parameters
        PRINT_INDEX,
        HIT, RESPONSE_TIME, PIT_SIZE, PIT_HIT, FORWARDING_RATE, FORWARDING_NUMBER, UTILIZATION, TOTAL_RATE, FORWARDING_PROB, DENSITY,TRACE_CATLOG,

        //The other parameters
        F, // total number of files
        C, // total cache size
        K, // total number of content providers
        L, // number of leaves
        Z, //hyperExponential
        T_ON, //ON-OF model
        T_OFF , //T_OFF=T_OFF*T_ON
        CLUSTER_SIZE, //number of caches which are in the same cluster
        NR_REQUESTS_PER_CP, NR_REQUESTS_PER_LEAF, MEAN_DOWNLOAD_TIME, NR_INNER_NODES, NR_LEAVES_PER_INNER, COOPERATION, COUPLING, CONSTRAINTS, LINK_DELAY, ZIPF, INITIAL_ACCESS_DELAY, ZDD, WRITE_RATE, HIT_RATE, PIT_HIT_RATE, MEAN_SERVICE_TIME
    }
}
