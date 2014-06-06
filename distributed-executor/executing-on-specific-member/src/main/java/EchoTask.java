import java.io.Serializable;

public class EchoTask implements Runnable, Serializable {
    private final String msg;

    public EchoTask(String msg) {
        this.msg = msg;
    }

    public void run() {
        System.out.println(msg);
    }
}
