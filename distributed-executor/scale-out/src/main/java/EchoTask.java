import java.io.Serializable;

public class EchoTask implements Runnable, Serializable {

    private final String msg;

    public EchoTask(String msg) {
        this.msg = msg;
    }

    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Echo: " + msg);
    }
}
