package hazelcast.platform.labs.machineshop;

public class MachineStatusEvent  {
    private String serialNum;
    private long eventTime;
    private int bitRPM;
    private short bitTemp;
    private int bitPositionX;
    private int bitPositionY;
    private int bitPositionZ;

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public int getBitRPM() {
        return bitRPM;
    }

    public void setBitRPM(int bitRPM) {
        this.bitRPM = bitRPM;
    }

    public short getBitTemp() {
        return bitTemp;
    }

    public void setBitTemp(short bitTemp) {
        this.bitTemp = bitTemp;
    }

    public int getBitPositionX() {
        return bitPositionX;
    }

    public void setBitPositionX(int bitPositionX) {
        this.bitPositionX = bitPositionX;
    }

    public int getBitPositionY() {
        return bitPositionY;
    }

    public void setBitPositionY(int bitPositionY) {
        this.bitPositionY = bitPositionY;
    }

    public int getBitPositionZ() {
        return bitPositionZ;
    }

    public void setBitPositionZ(int bitPositionZ) {
        this.bitPositionZ = bitPositionZ;
    }

    @Override
    public String toString() {
        return "MachineStatusEvent{" +
                "serialNum='" + serialNum + '\'' +
                ", timestamp=" + eventTime +
                ", bitRPM=" + bitRPM +
                ", bitTemp=" + bitTemp +
                ", bitPositionX=" + bitPositionX +
                ", bitBitPositionY=" + bitPositionY +
                ", bitPositionZ=" + bitPositionZ +
                '}';
    }

}
