package multitenant;

import com.hazelcast.spi.tenantcontrol.TenantControl;
import com.hazelcast.spi.tenantcontrol.TenantControlFactory;
import java.util.concurrent.atomic.AtomicReference;

public class MultiTenantControlFactory implements TenantControlFactory {
    static final AtomicReference<String> TENANT_CACHE = new AtomicReference<>();

    @Override
    public TenantControl saveCurrentTenant() {
        return new MultiTenantControl(TENANT_CACHE.getAndSet(null));
    }

    @Override
    public boolean isClassesAlwaysAvailable() {
        return false;
    }
}
