package guides.hazelcast.spring;

@SuppressWarnings("unused")
public class CommandResponse {

    private String value;
    private String podName;

    public CommandResponse() {
    }

    public CommandResponse(String value, String podName) {
        this.value  = value;
        this.podName = podName;
    }

    public String getValue() {
        return value;
    }
    public String getPodName() {
        return podName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }
}
