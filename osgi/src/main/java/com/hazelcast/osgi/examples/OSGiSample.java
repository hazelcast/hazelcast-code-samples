package com.hazelcast.osgi.examples;

import com.hazelcast.core.Hazelcast;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

/**
 * Demonstrates how to use Hazelcast's OSGI support through {@link com.hazelcast.osgi.HazelcastOSGiService}.
 */
public class OSGiSample {

    private static final String SAMPLE_BUNDLE_ID = "Sample Bundle";
    private static final String SAMPLE_BUNDLE_VERSION = "1.0.0";

    public static void main(String[] args) throws Exception {
        BundleContext bundleContext = null;
        Bundle sampleBundle = null;
        try {
            FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
            Map<String, String> frameworkConfigs = new HashMap<String, String>();
            frameworkConfigs.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");
            frameworkConfigs.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            frameworkConfigs.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_APP);
            Framework framework = frameworkFactory.newFramework(frameworkConfigs);

            // Start the OSGI framework
            framework.start();

            bundleContext = framework.getBundleContext();
            List<Bundle> installedBundles = new LinkedList<Bundle>();

            // Find the Hazelcast jar which is OSGI bundle also
            String bundleLocation = Hazelcast.class.getProtectionDomain().getCodeSource().getLocation().toString();

            System.out.println("Loading `Hazelcast` bundle from " + bundleLocation + "...");

            // Install Hazelcast bundle
            installedBundles.add(bundleContext.installBundle(bundleLocation));

            // Create a sample bundle which is activated by our sample activator
            InputStream sampleBundleInputStream = bundle()
                    .add(SampleActivator.class)
                    .set(Constants.BUNDLE_SYMBOLICNAME, SAMPLE_BUNDLE_ID)
                    .set(Constants.BUNDLE_VERSION, SAMPLE_BUNDLE_VERSION)
                    .set(Constants.BUNDLE_ACTIVATOR, SampleActivator.class.getName())
                    .build(withBnd());

            System.out.println("Loading sample bundle...");

            // Install our sample bundle
            sampleBundle = bundleContext.installBundle(SAMPLE_BUNDLE_ID, sampleBundleInputStream);
            installedBundles.add(sampleBundle);

            // Start installed bundles
            for (Bundle bundle : installedBundles) {
                System.out.println("Starting bundle " + bundle + "...");
                bundle.start();
            }
        } finally {
            if (bundleContext != null) {
                if (sampleBundle != null) {
                    // Stop our sample bundle first
                    System.out.println("Stopping bundle " + sampleBundle + "...");
                    sampleBundle.stop();
                }
                // Stop all bundles except sample bundle because it is already stopped above
                for (Bundle bundle : bundleContext.getBundles()) {
                    if (bundle != sampleBundle) {
                        System.out.println("Stopping bundle " + bundle + "...");
                        bundle.stop();
                    }
                }
            }
        }
    }
}
