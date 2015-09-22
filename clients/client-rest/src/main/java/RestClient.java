import com.hazelcast.util.EmptyStatement;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Hazelcast REST client.
 *
 * Reads a simple string value from a map.
 * Reads an object from a map.
 */
public class RestClient {

    // Base Hazelcast REST url
    // @see http://docs.hazelcast.org/docs/latest/manual/html/restclient.html
    private static final String HZ_REST_URL = "http://127.0.0.1:5701/hazelcast/rest";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(HZ_REST_URL);

        // querying map with String values
        Response stringResponse = target.path("/maps/simple/key1").request().get();
        String responseBody = stringResponse.readEntity(String.class);
        System.out.println("Value for key1 is " + responseBody);

        // querying map with Person object values
        Response objectResponse = target.path("/maps/object/key1").request().get();
        byte[] entity = objectResponse.readEntity(byte[].class);
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(entity);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);

            Person person = (Person) objectInputStream.readObject();
            System.out.println("Object for key1 is " + person.toString());
        } finally {
            closeQuietly(objectInputStream);
            closeQuietly(byteArrayInputStream);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                EmptyStatement.ignore(ignored);
            }
        }
    }
}
