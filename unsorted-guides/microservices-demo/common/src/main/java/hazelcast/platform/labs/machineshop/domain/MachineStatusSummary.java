package hazelcast.platform.labs.machineshop.domain;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class MachineStatusSummary  {
    private String serialNumber;
    private short  averageBitTemp10s;
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
        return "MachineStatusSummary{" +
                "serialNumber='" + serialNumber + '\'' +
                ", averageBitTemp10s=" + averageBitTemp10s +
                '}';
    }

    public static class Serializer implements CompactSerializer<MachineStatusSummary>{

        @Override
        public MachineStatusSummary read(CompactReader compactReader) {
            MachineStatusSummary result = new MachineStatusSummary();
            result.setSerialNumber(compactReader.readString("serialNumber"));
            result.setAverageBitTemp10s(compactReader.readInt16("averageBitTemp10s"));
            return result;
        }

        @Override
        public void write(CompactWriter compactWriter, MachineStatusSummary machineStatusSummary) {
            compactWriter.writeString("serialNumber", machineStatusSummary.getSerialNumber());
            compactWriter.writeInt16("averageBitTemp10s", machineStatusSummary.getAverageBitTemp10s());
        }

        @Override
        public String getTypeName() {
            return MachineStatusSummary.class.getName();
        }

        @Override
        public Class<MachineStatusSummary> getCompactClass() {
            return MachineStatusSummary.class;
        }
    }
}
