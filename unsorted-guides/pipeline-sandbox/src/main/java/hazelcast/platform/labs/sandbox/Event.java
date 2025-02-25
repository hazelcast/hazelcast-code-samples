package hazelcast.platform.labs.sandbox;

import java.io.Serializable;

public class Event implements Serializable {
    public long timestamp;
    public String label;

    public Event(){
    }
    public Event(long timestamp, String label) {
        if (label.length() != 3) throw new RuntimeException("label  length must be 3");
        this.timestamp = timestamp;
        this.label = label;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        // need this to be a characters long
        String t = "" + timestamp;
        t = t.substring(t.length() - 4);
        return t + " " + label;
    }
}
