import java.io.Serializable;

public final class Article implements Serializable {

    private final String name;

    public Article(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
