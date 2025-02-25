package hazelcast.platform.labs.machineshop;

import java.io.Serializable;
import java.util.Objects;

public class Status implements Serializable {
    private String status;

    public Status(){
        this.status = null;
    }

    /**
     * Compares newStatus with the existing status
     * @param newStatus
     * @return true if the status has changed, false otherwise
     */
    public boolean checkAndSet(String newStatus){
        if (status != null && status.equals(newStatus)) {
            return false;
        } else {
            status = newStatus;
            return true;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status1 = (Status) o;
        return status.equals(status1.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}
