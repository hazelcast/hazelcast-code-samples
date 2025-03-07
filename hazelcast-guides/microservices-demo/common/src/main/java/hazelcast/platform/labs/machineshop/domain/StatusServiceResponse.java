package hazelcast.platform.labs.machineshop.domain;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class StatusServiceResponse  {
    private String serialNumber;
    private Short  averageBitTemp10s;
    private Short  warningTemp;
    private Short criticalTemp;
    private String status;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Short getAverageBitTemp10s() {
        return averageBitTemp10s;
    }

    public void setAverageBitTemp10s(Short averageBitTemp10s) {
        this.averageBitTemp10s = averageBitTemp10s;
    }

    public Short getWarningTemp() {
        return warningTemp;
    }

    public void setWarningTemp(Short warningTemp) {
        this.warningTemp = warningTemp;
    }

    public Short getCriticalTemp() {
        return criticalTemp;
    }

    public void setCriticalTemp(Short criticalTemp) {
        this.criticalTemp = criticalTemp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusServiceResponse{" +
                "serialNumber='" + serialNumber + '\'' +
                ", averageBitTemp10s=" + averageBitTemp10s +
                ", warningTemp=" + warningTemp +
                ", criticalTemp=" + criticalTemp +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Serializer implements CompactSerializer<StatusServiceResponse> {

        @Override
        public StatusServiceResponse read(CompactReader compactReader) {
            StatusServiceResponse result = new StatusServiceResponse();
            result.setSerialNumber(compactReader.readString("serialNumber"));
            result.setAverageBitTemp10s(compactReader.readInt16("averageBitTemp10s"));
            result.setWarningTemp(compactReader.readInt16("warningTemp"));
            result.setCriticalTemp(compactReader.readInt16("criticalTemp"));
            result.setStatus(compactReader.readString("status"));
            return result;
        }

        @Override
        public void write(CompactWriter compactWriter, StatusServiceResponse statusServiceResponse) {
            compactWriter.writeString("serialNumber", statusServiceResponse.getSerialNumber());
            compactWriter.writeInt16("averageBitTemp10s", statusServiceResponse.getAverageBitTemp10s());
            compactWriter.writeInt16("warningTemp", statusServiceResponse.getWarningTemp());
            compactWriter.writeInt16("criticalTemp", statusServiceResponse.getCriticalTemp());
            compactWriter.writeString("status", statusServiceResponse.getStatus());
        }

        @Override
        public String getTypeName() {
            return StatusServiceResponse.class.getName();
        }

        @Override
        public Class<StatusServiceResponse> getCompactClass() {
            return StatusServiceResponse.class;
        }
    }
}
