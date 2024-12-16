package com.hazelcast.guide.controller;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is optional; it's used only to check cluster size to check if cluster already have all nodes connected
 * in the tests.
 */
@RestController
public class ClusterController {

    private final HazelcastInstance hazelcastInstance;

    public ClusterController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @GetMapping("/clusterSize")
    public int getClusterSize() {
        return hazelcastInstance.getCluster().getMembers().size();
    }
}
