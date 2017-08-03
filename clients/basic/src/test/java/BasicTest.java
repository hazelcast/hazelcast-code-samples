import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BasicTest {

    @Test
    public void testBasics() throws Exception {
        Member member = new Member();
        Client client = new Client();

        String string = "Hello!";
        client.put(string);

        assertEquals(string, member.take());
    }
}
