package edu.sharif.ce.mahmadi.caching;

import edu.sharif.ce.mahmadi.server.Event;
import edu.sharif.ce.mahmadi.simulation.Constants;

public class RequestEvent extends Event {

    private int fId;
    private int flowId; //for service differentiation between contents
    private double downloadStartTime;
    private int currentCache; //Id of the cache now responsible for this request
    private Constants.MESSAGE messageType;

    public RequestEvent(int cId, Constants.EVENT_TYPE mtype, Constants.MESSAGE message, double mtriggerTime) {
        super(mtype, message.ordinal() + 1, mtriggerTime, mtriggerTime);
        this.currentCache = cId;
        this.messageType = message;
        this.downloadStartTime = 0;
        this.flowId = 1;
    }

    public RequestEvent(int fId, int cId, Constants.EVENT_TYPE mtype, Constants.MESSAGE message, double mtriggerTime) {
        super(mtype, message.ordinal() + 1, mtriggerTime, mtriggerTime);
        this.fId = fId;
        this.currentCache = cId;
        this.messageType = message;
        this.downloadStartTime = 0;
        this.flowId = 1;
    }

    public RequestEvent(int fId, int cId, Constants.MESSAGE message, double mtriggerTime) {
        super(Constants.EVENT_TYPE.ARRIVAL, message.ordinal() + 1, mtriggerTime, mtriggerTime);
        this.fId = fId;
        this.currentCache = cId;
        this.messageType = message;
        this.downloadStartTime = 0;
    }

    public int getCurrentCache() {
        return currentCache;
    }

    public void setCurrentCache(int currentCache) {
        this.currentCache = currentCache;
    }

    public Constants.MESSAGE getMessageType() {
        return messageType;
    }

    public void setMessageType(Constants.MESSAGE messageType) {
        this.messageType = messageType;
        this.classId = messageType.ordinal() + 1;
    }

    /*change*/
    public int getFlowId() {
        //return classId;
        //return fId;
        //return (fId<800)? 2: 1;
        return flowId;
    }

    public int getfId() {
        return fId;
    }

    @Override
    public String toString() {
        return "REQUEST_EVENT [type=" + type + ", Id=" + this.getId() + ", fId=" + fId + ", cId=" + currentCache + ", triggerTime=" + this.getTriggerTime() + ", birthTime=" + this.getBirthTime() + "]";
    }

    public double getDownloadStartTime() {
        return downloadStartTime;
    }

    public void setDownloadStartTime(double startDownloadTime) {
        this.downloadStartTime = startDownloadTime;
    }

}
