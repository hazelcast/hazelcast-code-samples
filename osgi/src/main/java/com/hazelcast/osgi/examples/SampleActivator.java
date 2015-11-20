package com.hazelcast.osgi.examples;

import com.hazelcast.config.Config;
import com.hazelcast.osgi.HazelcastOSGiInstance;
import com.hazelcast.osgi.HazelcastOSGiService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * {@link org.osgi.framework.BundleActivator} implementation for OSGI sample which is activated by our sample bundle.
 */
@SuppressWarnings("unused")
public class SampleActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting activator " + this + " in bundle " + context.getBundle() + "...");

        // Find the service reference of `HazelcastOSGiService` instance
        ServiceReference serviceRef =
                context.getServiceReference(HazelcastOSGiService.class.getName());
        if (serviceRef == null) {
            throw new IllegalStateException("There is no registered `HazelcastOSGiService`!");
        }

        // Get the `HazelcastOSGiService` over service reference
        HazelcastOSGiService hazelcastOsgiService = (HazelcastOSGiService) context.getService(serviceRef);

        // Get the default Hazelcast instance owned by `hazelcastOsgiService`
        // Returns null if `HAZELCAST_OSGI_START` is not enabled
        HazelcastOSGiInstance defaultInstance = hazelcastOsgiService.getDefaultHazelcastInstance();

        // Creates a new Hazelcast instance with default configurations as owned by `hazelcastOsgiService`
        HazelcastOSGiInstance newInstance1 = hazelcastOsgiService.newHazelcastInstance();

        // Creates a new Hazelcast instance with specified configuration as owned by `hazelcastOsgiService`
        Config config = new Config();
        config.setInstanceName("OSGI-Instance");

        HazelcastOSGiInstance newInstance2 = hazelcastOsgiService.newHazelcastInstance(config);

        // Gets the Hazelcast instance with name `OSGI-Instance` which is `newInstance2` created below
        HazelcastOSGiInstance instance = hazelcastOsgiService.getHazelcastInstanceByName("OSGI-Instance");

        // Shuts down the Hazelcast instance with name `OSGI-Instance` which is `newInstance2`
        hazelcastOsgiService.shutdownHazelcastInstance(instance);

        // Print all active Hazelcast instances owned by `hazelcastOsgiService`
        for (HazelcastOSGiInstance osgiInstance : hazelcastOsgiService.getAllHazelcastInstances()) {
            System.out.println(osgiInstance);
        }

        // Shuts down all Hazelcast instances owned by `hazelcastOsgiService`
        hazelcastOsgiService.shutdownAll();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping activator " + this + " in bundle " + context.getBundle() + "...");
    }
}
