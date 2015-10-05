package com.hazelcast.samples.entrylistener.handoff.examples;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Bootstraps a Hazelcast Cluster Member via Spring Context.  Then puts texts of 3 books into a map.  Each put
 * results in an EntryListener ADDED event which in turn calls onto the EntryEventService delegate.  The EntryEventService
 * called uses a WordCountEntryEventProcessor to count the number of words in each book added to the map.
 * <P>
 * The EntryEventService used in this example is the ThreadPoolEntryEventService.
 * <P>
 * Note: In the Spring Context the wiring of the EntryEventProcessors into the EntryEventService is carried
 * out by the EntryEventProcessorRegistra
 */
public class HazelcastBookCountOnWordsExample {

    private static String[] books = {"Ulysses","Metamorphosis","AdventuresOfHuckleberryFinn"};

    public static void main(String[] args) throws IOException {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");

        HazelcastInstance hazelcastInstance = applicationContext.getBean("instance", HazelcastInstance.class);

        IMap<Object, Object> testMap = hazelcastInstance.getMap("testMap");

        for(String book:books){
            URL resource = HazelcastBookCountOnWordsExample.class.getResource("/"+book+".txt");
            byte[] encoded = Files.readAllBytes(Paths.get(resource.getPath()));
            String text = new String(encoded, Charset.forName("UTF-8"));
            System.out.println("putting book : " + book);
            testMap.put(book,text);
        }

    }
}
