import java.io.Serializable;

class DummyObject implements Serializable {

    transient Thread trans = new Thread();

    private String ser = "someValue";

    @Override
    public String toString() {
        return "DummyObject{"
                + "ser='" + ser + '\''
                + ", trans=" + trans
                + '}';
    }
}
