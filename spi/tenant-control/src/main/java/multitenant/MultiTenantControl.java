package multitenant;

import com.hazelcast.spi.tenantcontrol.TenantControl;

import java.io.Closeable;

import static multitenant.MultiTenantClassLoader.CLASS_LOADER_PER_PREFIX;

public class MultiTenantControl implements TenantControl {

    private final String cacheName;

    MultiTenantControl(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public Closeable setTenant(boolean createRequestScope) {
        ClassLoader original = null;
        if (cacheName != null) {
            System.out.println("Setting class loader for cache " + cacheName + " / " + CLASS_LOADER_PER_PREFIX.get(cacheName));
            original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(CLASS_LOADER_PER_PREFIX.get(cacheName));
        }
        return new TenantCloseable(original);
    }

    @Override
    public void unregister() {
        // nothing to do
    }
}
