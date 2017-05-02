package com.hazelcast.dynacache.client;

import com.example.SomeClass;
import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MainServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            InitialContext ic = new InitialContext();

            DistributedMap map = (DistributedMap) ic.lookup("some-hz-map");
            System.out.println("Found 'some-hz-map' via JNDI: " + map);

            Object putResult = map.put("the key", "the value", 1000, 1000, EntryInfo.SHARED_PUSH_PULL,
                    new Object[]{"dep1", "dep2"});
            resp.getWriter().write("PutResult: " + putResult + "\n");

            map.put("the key 2", "the value 2", 1000, 1000, EntryInfo.SHARED_PUSH_PULL,
                    new Object[]{"dep1"});

            resp.getWriter().write("the key => " + map.get("the key") + "\n");
            resp.getWriter().write("the key 2 => " + map.get("the key 2") + "\n");

            map.invalidate("the key", false);

            resp.getWriter().write("Invalidation took place...\n");

            resp.getWriter().write("the key => " + map.get("the key") + "\n");
            resp.getWriter().write("the key2 => " + map.get("the key 2") + "\n");

            SomeClass someObject = new SomeClass();
            someObject.setField1("field 1");
            someObject.setField2("field 2");
            map.put("key1", someObject, 1, 0, 1, null);

            someObject.setField2("field2updated");

            map.put("key1", someObject, 1, 0, 1, null);
            SomeClass someObjectRetrieved = (SomeClass) map.get("key1");
            resp.getWriter().write(someObjectRetrieved.toString());
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
