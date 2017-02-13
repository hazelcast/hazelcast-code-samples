package com.hazelcast.wan.batch.map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Random;
import java.util.Scanner;

import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * See README for details.
 */
public class EnterpriseMapWanReplicationClusterA {

    private static HazelcastInstance clusterA;

    public static void main(String[] args) {
        initClusters();
        waitUntilClusterSafe();
        Scanner reader = new Scanner(System.in);
        IMap<Object, Object> map = clusterA.getMap("default");
        System.out.println("Cluster is ready now.");
        System.out.println("write \"help\" for the command lists:");
        while (true) {
            sleepMillis(100);
            System.out.println("Command:");
            String command = reader.nextLine();
            if (command.equals("help")) {
                printHelpCommands();
            }
            if (command.equals("size")) {
                System.out.println("map size: " + map.size());
            }
            String key;
            if (command.equals("get")) {
                key = reader.nextLine();
                System.out.println(map.get(key));
            }
            if (command.startsWith("put ")) {
                key = command.split(" ")[1];
                String value = command.split(" ")[2];
                System.out.println(map.put(key, value));
            }
            if (command.startsWith("putmany")) {
                key = command.split(" ")[1];
                int start = new Random().nextInt();
                for (int i = start; i < start + Integer.parseInt(key); i++) {
                    String kv = Integer.toString(i);
                    map.put(kv, kv);
                }
            }
        }
    }

    private static void printHelpCommands() {
        System.out.println("Commands:\n"
                + "1) get [key]\n"
                + "2) size\n"
                + "3) put [key] [value]\n"
                + "4) putmany [number]\n");
    }

    private static void waitUntilClusterSafe() {
        while (!clusterA.getPartitionService().isClusterSafe()) {
            sleepMillis(100);
        }
    }

    private static void initClusters() {
        clusterA = Hazelcast.newHazelcastInstance();
    }
}
