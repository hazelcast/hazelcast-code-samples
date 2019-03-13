package multitenant;

import com.hazelcast.spi.tenantcontrol.DestroyEventContext;
import com.hazelcast.spi.tenantcontrol.TenantControl;
import com.hazelcast.spi.tenantcontrol.TenantControlFactory;

public class MultiTenantControlFactory implements TenantControlFactory {

    @Override
    public TenantControl saveCurrentTenant(DestroyEventContext event) {
        String cacheName = event.getDistributedObjectName();
        return new MultiTenantControl(cacheName);
    }
}
