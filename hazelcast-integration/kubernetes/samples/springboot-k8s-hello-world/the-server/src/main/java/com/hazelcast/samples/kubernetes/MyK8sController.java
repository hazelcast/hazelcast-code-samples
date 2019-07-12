package com.hazelcast.samples.kubernetes;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Provide a HTTP endpoint to test if this Hazelcast server is happy.
 * Kubernetes will use this to test that the JVM is up.
 * </p>
 * <p>
 * The Hazelcast client provides a similar but not identical
 * implementation. Kubernetes only cares for HTTP 200.
 * </p>
 * <p>
 * You can test this with:
 * <pre>
 * curl -v http://localhost:8081/k8s
 * </pre>
 * substituting the port you've selected if not 8081.
 * </p>
 */
@RestController
public class MyK8sController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyK8sController.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * @return {@code true} if good, exception otherwise
     */
    @GetMapping("/k8s")
    public String k8s() {
        LOGGER.info("k8s()");

        ClusterState clusterState = this.hazelcastInstance.getCluster().getClusterState();

        if (clusterState == ClusterState.ACTIVE) {
            return Boolean.TRUE.toString();
        } else {
            throw new RuntimeException("ClusterState==" + clusterState);
        }
    }

}
