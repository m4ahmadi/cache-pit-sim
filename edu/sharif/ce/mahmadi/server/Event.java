/***
 * @author mahdieh
 * @time utilityBasedCaching
 */

package edu.sharif.ce.mahmadi.server;

import edu.sharif.ce.mahmadi.simulation.Constants.EVENT_TYPE;

public class Event implements Comparable<Event> {

    public static int lastEventId = 0;
    protected EVENT_TYPE type;
    private int Id;
    private double triggerTime;
    private double birthTime;
    private double startTime;
    private double duration;
    protected int classId;



    public Event(EVENT_TYPE mtype, int classId, double mBirthTime, double mtriggerTime) {
        lastEventId++;
        Id = lastEventId;
        this.triggerTime = mtriggerTime;
        this.birthTime = mBirthTime;
        this.startTime = mBirthTime;
        this.duration = 0;
        this.type = mtype;
        this.classId = classId;

    }

    public Event update(EVENT_TYPE mtype, double mtriggerTime, double duration) {
        this.triggerTime = mtriggerTime;
        this.duration = duration;
        this.type = mtype;
        return this;
    }

    public int getId() {
        return Id;
    }

    public EVENT_TYPE getType() {
        return type;
    }

    public void setType(EVENT_TYPE type) {
        this.type = type;
    }

    public double getTriggerTime() {
        return this.triggerTime;
    }

    public void setTriggerTime(double time) {
        this.triggerTime = time;
    }

    public int getClassId() {
        return classId;
    }

    public double getBirthTime() {
        return this.birthTime;
    }

    public void setBirthTime(double birthTime) {
        this.birthTime = birthTime;
    }

    public double getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Event other = (Event) obj;
        if (Id != other.Id) return false;
        return true;
    }

    @Override
    public String toString() {
        return "EVENT [type=" + type + ", Id=" + Id + ", triggerTime=" + triggerTime + "]";
    }

    @Override
    public int compareTo(Event otherEvent) {
        if (triggerTime < otherEvent.triggerTime) {
            return -1;
        } else if (triggerTime > otherEvent.triggerTime) {
            return 1;
        } else {
            return 0;
        }
    }

    public double getStartTime() {
        return startTime;
    }


}
