package multitenant;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.tenantcontrol.DestroyEventContext;
import com.hazelcast.spi.tenantcontrol.TenantControl;
import com.hazelcast.spi.tenantcontrol.Tenantable;
import java.io.IOException;


import static multitenant.MultiTenantClassLoader.CLASS_LOADER_PER_PREFIX;

public class MultiTenantControl implements TenantControl {

    private String cacheName;
    private DestroyEventContext destroyEventContext;

    @Override
    public Closeable setTenant() {
        ClassLoader original;
        if (cacheName != null) {
            System.out.println("Setting class loader for cache " + cacheName + " / " + CLASS_LOADER_PER_PREFIX.get(cacheName));
            original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(CLASS_LOADER_PER_PREFIX.get(cacheName));
        } else {
            original = null;
        }
        return new TenantCloseable(original)::close;
    }

    @Override
    public void registerObject(DestroyEventContext destroyEventContext) {
        this.destroyEventContext = destroyEventContext;
    }

    @Override
    public void unregisterObject() {
        destroyEventContext.tenantUnavailable();
        destroyEventContext = () -> {};
    }

    @Override
    public boolean isAvailable(Tenantable tenantable) {
        return true;
    }

    @Override
    public void clearThreadContext() {
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(cacheName);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        cacheName = in.readUTF();
    }
}
