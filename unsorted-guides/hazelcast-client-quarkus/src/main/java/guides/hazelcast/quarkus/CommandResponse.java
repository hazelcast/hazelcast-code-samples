package guides.hazelcast.quarkus;

@SuppressWarnings("unused")
public class CommandResponse {

    private String value;
    private String containerName;

    public CommandResponse() {
    }

    public CommandResponse(String value, String containerName) {
        this.value = value;
        this.containerName = containerName;
    }

    public String getValue() {
        return value;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
}
