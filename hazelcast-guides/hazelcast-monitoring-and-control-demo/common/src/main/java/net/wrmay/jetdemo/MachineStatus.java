package net.wrmay.jetdemo;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class MachineStatus {
    private String serialNum;

    private long timestamp;
    private int bitRPM;
    private int bitTemp;
    private int bitPositionX;
    private int bitBitPositionY;
    private int bitPositionZ;

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getBitRPM() {
        return bitRPM;
    }

    public void setBitRPM(int bitRPM) {
        this.bitRPM = bitRPM;
    }

    public int getBitTemp() {
        return bitTemp;
    }

    public void setBitTemp(int bitTemp) {
        this.bitTemp = bitTemp;
    }

    public int getBitPositionX() {
        return bitPositionX;
    }

    public void setBitPositionX(int bitPositionX) {
        this.bitPositionX = bitPositionX;
    }

    public int getBitBitPositionY() {
        return bitBitPositionY;
    }

    public void setBitBitPositionY(int bitBitPositionY) {
        this.bitBitPositionY = bitBitPositionY;
    }

    public int getBitPositionZ() {
        return bitPositionZ;
    }

    public void setBitPositionZ(int bitPositionZ) {
        this.bitPositionZ = bitPositionZ;
    }

    @Override
    public String toString() {
        return "MachineStatus{" +
                "serialNum='" + serialNum + '\'' +
                ", timestamp=" + timestamp +
                ", bitRPM=" + bitRPM +
                ", bitTemp=" + bitTemp +
                ", bitPositionX=" + bitPositionX +
                ", bitBitPositionY=" + bitBitPositionY +
                ", bitPositionZ=" + bitPositionZ +
                '}';
    }

}
