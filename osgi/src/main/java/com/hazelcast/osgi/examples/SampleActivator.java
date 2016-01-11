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
class SampleActivator implements BundleActivator {

    private HazelcastOSGiService hazelcastOsgiService;
    private HazelcastOSGiInstance hazelcastOSGiInstance;

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
        hazelcastOsgiService = (HazelcastOSGiService) context.getService(serviceRef);

        // Get the default Hazelcast instance owned by `hazelcastOsgiService`
        // Returns null if `HAZELCAST_OSGI_START` is not enabled
        HazelcastOSGiInstance defaultInstance = hazelcastOsgiService.getDefaultHazelcastInstance();
        System.out.println("Default Hazelcast instance (available when `"
                + HazelcastOSGiService.HAZELCAST_OSGI_START + "` flag is enabled): " + defaultInstance);

        // Create a new Hazelcast instance with default configurations as owned by `hazelcastOsgiService`
        HazelcastOSGiInstance newInstance1 = hazelcastOsgiService.newHazelcastInstance();
        System.out.println("New Hazelcast OSGI instance with default config: " + newInstance1);

        // Create a new Hazelcast instance with specified configuration as owned by `hazelcastOsgiService`
        hazelcastOSGiInstance = hazelcastOsgiService.newHazelcastInstance(new Config("OSGI-Instance"));
        System.out.println("New Hazelcast OSGI instance with specified config (name=`OSGI-Instance`): " + hazelcastOSGiInstance);

        // Gets the Hazelcast instance with name `OSGI-Instance` which is `newInstance2` created below
        HazelcastOSGiInstance instance = hazelcastOsgiService.getHazelcastInstanceByName("OSGI-Instance");
        System.out.println("Hazelcast OSGI instance by name `OSGI-Instance`: " + instance);

        System.out.println("Here are all Hazelcast OSGI instances:");
        // Print all active Hazelcast instances owned by `hazelcastOsgiService`
        for (HazelcastOSGiInstance osgiInstance : hazelcastOsgiService.getAllHazelcastInstances()) {
            System.out.println("\t- " + osgiInstance);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping activator " + this + " in bundle " + context.getBundle() + "...");

        System.out.println("Shutting down Hazelcast OSGI instance: " + hazelcastOSGiInstance);
        hazelcastOsgiService.shutdownHazelcastInstance(hazelcastOSGiInstance);

        System.out.println("Shutting down all Hazelcast OSGI instances ...");
        // Shuts down all Hazelcast instances owned by `hazelcastOsgiService`
        hazelcastOsgiService.shutdownAll();
    }
}
