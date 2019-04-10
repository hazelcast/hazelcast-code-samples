package your.company.name;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>A simple command line interpreter. There are lots of
 * libraries that would do this for us, but don't use it
 * to keep the dependencies to a minimum (none!).
 * </p>
 */
public class InputReader implements Closeable {

    public static final String CLUSTER_NAME =
            "the name of the cluster";
    public static final String CLUSTER_PASSWORD =
            "the password of the cluster";
    public static final String CLUSTER_DISCOVERY_TOKEN =
            "the discovery token for the cluster";

    private BufferedReader bufferedReader;
    private InputStreamReader inputStreamReader;

    InputReader() {
        this.inputStreamReader = new InputStreamReader(System.in);
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
    }

    /**
     * <p>Read the value for a field from the input.
     * </p>
     *
     * @param description Text to prompt the user.
     * @return A non-null String
     */
    public String read(String description) {

        System.out.println("Please provide a value for " + description
                + ", or 'QUIT' to abandon");
        System.out.printf("$ ");

        String line;

        try {
            while ((line = this.bufferedReader.readLine()) != null) {
                if (line.length() > 0) {
                    return line.equalsIgnoreCase("QUIT") ? "" : line;
                }
                System.out.printf("$ ");
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }

        return "";
    }

    @Override
    public void close() throws IOException {
        this.bufferedReader.close();
        this.inputStreamReader.close();
    }
}
