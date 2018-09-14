package com.vreddy.demo.hazelcast;

import com.hazelcast.com.eclipsesource.json.ParseException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
@ComponentScan(basePackages = "com.target")
public class SetController {

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Value("${server.port}")
    private Integer serverPort;

    ISet<String> set = null;

    @Autowired
    ItemDeltaEntryListener itemDeltaEntryListener;

    @Service
    static class ItemDeltaEntryListener implements ItemListener<String> {

        @Override
        public void itemAdded(ItemEvent<String> item) {
            //Received delta for items. Invalidate them in cache.
            List<String> itemList = new ArrayList<String>();
            String value =  item.getItem();
            System.out.println("Item added: " + value);
        }

        @Override
        public void itemRemoved(ItemEvent<String> item) {
            System.out.println("Cleared");
        }
    }

    public ISet<String> getDeltaItemsSet() {
        return this.set;
    }

    @RequestMapping(value = "/set", method = { RequestMethod.POST })
    public ResponseEntity<NormalClass> invalidateCache(@RequestParam String dataKeys) throws ParseException {
        String[] dataKeyArray = dataKeys.split(",");
        List<String> dataKeyList = new ArrayList<String>();
        for(String dataKey : dataKeyArray){
            dataKeyList.add(dataKey);
        }
        triggerUpdate(dataKeyList);
        NormalClass normal = new NormalClass();
        normal.setName("Supp");
        return new ResponseEntity<NormalClass>(normal, HttpStatus.OK);
    }

    public void triggerUpdate(List<String> data) {
        //Add items to the ISet. Triggers item added event listener
        getDeltaItemsSet().addAll(data);
        System.out.println(serverPort);
        //Remove all the items, as we have invalidated the cache
        getDeltaItemsSet().clear();
        System.out.println("Clear: " + serverPort);
    }

    @PostConstruct
	public void constructItemSet() {
        this.set = hazelcastInstance.getSet("setOne");
        this.set.addItemListener(itemDeltaEntryListener, true);
    }
}