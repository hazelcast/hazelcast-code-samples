package hazelcast.platform.labs.machineshop.domain;

public class MachineStatus  {
    private String serialNumber;
    private short  averageBitTemp10s;
    private long eventTime;

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public short getAverageBitTemp10s() {
        return averageBitTemp10s;
    }

    public void setAverageBitTemp10s(short averageBitTemp10s) {
        this.averageBitTemp10s = averageBitTemp10s;
    }

    @Override
    public String toString() {
        return "MachineStatus{" +
                "serialNumber='" + serialNumber + '\'' +
                ", averageBitTemp10s=" + averageBitTemp10s +
                ", eventTime=" + eventTime +
                '}';
    }
}
