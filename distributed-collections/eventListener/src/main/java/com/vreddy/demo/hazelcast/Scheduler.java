package com.vreddy.demo.hazelcast;

import com.hazelcast.core.*;
import com.hazelcast.monitor.LocalMapStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Created by z002rgw on 8/27/17.
 */
@Component
public class Scheduler {

    @Lazy
    @Autowired
    HazelcastInstance instance;

    public void Scheduler(){

    }

    @Scheduled(fixedDelay = 3000)
    public void run(){
        IMap<String, Integer> map = instance.getMap("mapOne");
        //IMap<String, Integer> map = instance.getSet("mapOne");
        PartitionService partitionService = instance.getPartitionService();

        //HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();
        LocalMapStats stats = map.getLocalMapStats();

        System.out.println("Backupcount: " + stats.getBackupCount());
        //System.out.println("Hits: " + stats.getHits());
        System.out.println("Heap cost: " + stats.getHeapCost());


        //System.out.println("Entries in this map:");
        Set<Partition> partitions = partitionService.getPartitions();
        for(Partition partition1 : partitions){
            System.out.println(partition1.getOwner() + " " + partition1.getPartitionId());
        }
        for(String key : map.keySet()){
            System.out.println(key);
        }
    }

    @PostConstruct
    public void constructMap(){
        System.out.println("Post construct is called");
        IMap<String, Integer> map = instance.getMap("mapOne");
        int n = (int) (Math.random() * 100 + 1);
        System.out.println(n);
        System.out.println(map);

        for(int i = 0; i < n; i++){
            map.set("S" + i, i);
        }
    }
}