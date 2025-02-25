package hazelcast.platform.labs.machineshop.domain;

import com.hazelcast.config.EventJournalConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;

public class Names {

    public static final String PROFILE_MAP_NAME = "machine_profiles";

    public static final String SYSTEM_ACTIVITIES_MAP_NAME = "system_activities";

    public static final String MACHINE_STATUS_MAP_NAME = "machine_status";

    public static final String MACHINE_PROFILE_TYPE_NAME = "hazelcast.platform.labs.machineshop.domain.MachineProfile";
    public static final String MACHINE_STATUS_TYPE_NAME = "hazelcast.platform.labs.machineshop.domain.MachineStatus";

    /*
     * This method, though not currently used, could be used to configure a cloud instance of Hazelcast
     * where the initial configuration is not under the user's control
     */
    public static class ProfileMapConfigurationTask implements Runnable, HazelcastInstanceAware, Serializable {

        private transient HazelcastInstance hz;
        @Override
        public void run() {
            hz.getConfig().addMapConfig(new MapConfig(PROFILE_MAP_NAME)
                    .setInMemoryFormat(InMemoryFormat.BINARY)
                    .setBackupCount(1));
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
            this.hz = hazelcastInstance;
        }
    }

}
