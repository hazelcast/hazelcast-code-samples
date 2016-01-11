import java.io.Serializable;

public final class Pair implements Serializable {

    private final String significant;
    private final String insignificant;

    public Pair(String significant, String insignificant) {
        this.significant = significant;
        this.insignificant = insignificant;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (this == thatObj) {
            return true;
        }
        if (thatObj == null || getClass() != thatObj.getClass()) {
            return false;
        }
        Pair that = (Pair) thatObj;
        return significant.equals(that.significant);
    }

    @Override
    public int hashCode() {
        return significant.hashCode() + 31 * insignificant.hashCode();
    }
}
