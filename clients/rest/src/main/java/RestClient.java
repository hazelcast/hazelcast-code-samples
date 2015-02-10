import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Rest client
 * <ul>
 *     <li> reads simple string value from map</li>
 *     <li> read object from map </li>
 * </ul> 
 *
 * @since 2/6/15
 */
public class RestClient {

    // Base Hazelcast REST url
    // @see http://docs.hazelcast.org/docs/latest/manual/html/restclient.html
    final static String HZ_REST_URL = "http://127.0.0.1:5701/hazelcast/rest";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(HZ_REST_URL);

        // querying map with String values
        final Response response1 = target.path("/maps/simple/key1").request().get();
        final String responseBody = response1.readEntity(String.class);
        System.out.println("Value for key1 is " + responseBody);

        // querying map with Person object values
        final Response response2 = target.path("/maps/object/key1").request().get();
        ObjectInputStream objectInputStream = new ObjectInputStream(
            new ByteArrayInputStream(response2.readEntity(byte[].class)));
        final Person person = (Person) objectInputStream.readObject();
        System.out.println("Object for key1 is " + person.toString());
    }
}

