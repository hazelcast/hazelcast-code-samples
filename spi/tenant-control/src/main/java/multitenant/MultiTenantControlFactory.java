package multitenant;

import com.hazelcast.spi.tenantcontrol.TenantControl;
import com.hazelcast.spi.tenantcontrol.TenantControlFactory;

public class MultiTenantControlFactory implements TenantControlFactory {

    @Override
    public TenantControl saveCurrentTenant() {
        return new MultiTenantControl();
    }

    @Override
    public boolean isClassesAlwaysAvailable() {
        return false;
    }
}
