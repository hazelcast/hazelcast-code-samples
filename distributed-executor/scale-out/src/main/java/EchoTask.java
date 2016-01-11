import java.io.Serializable;

class EchoTask implements Runnable, Serializable {

    private final String msg;

    EchoTask(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("echo:" + msg);
    }
}
