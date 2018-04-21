/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.hazelcast.config.FlakeIdGeneratorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

/**
 * This sample shows correct way of determing the ID offset when migrating from
 * old {@link IdGenerator} to {@link FlakeIdGenerator}.
 */
public class FlakeIdMigrationSample {
    private static final String GENERATOR_NAME = "myGenerator";

    public static void main(String[] args) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        IdGenerator oldGenerator = instance.getIdGenerator(GENERATOR_NAME);
        // Simulate we'got this far with the old IdGenerator
        oldGenerator.init(1000000000000000000L);

        // Create temporary Flake ID generator - we don't want to create the target generator
        // before we configure it.
        FlakeIdGenerator tmpGenerator = instance.getFlakeIdGenerator(GENERATOR_NAME + "__tmp");

        // Calculate the offset. Limit it to 0, if Flake ID generator has larger value
        // 1L << 38 is the reserve for about 1 minute worth of IDs. We need it because the IDs from
        // Flake generator are only roughly ordered.
        long idOffset = Math.max(0, oldGenerator.newId() - tmpGenerator.newId() + (1L << 38));
        System.out.println("idOffset=" + idOffset);

        // if the offset is larger than 0, configure it through dynamic config
        if (idOffset > 0) {
            System.out.println("Don't forget to add the idOffset to static configurations so that the value is preserved "
                    + "on cluster restart!");
            instance.getConfig().addFlakeIdGeneratorConfig(new FlakeIdGeneratorConfig(GENERATOR_NAME)
                    .setIdOffset(idOffset));
        }
        tmpGenerator.destroy();
        tmpGenerator = null;

        // Now we can create our target generator
        FlakeIdGenerator newGenerator = instance.getFlakeIdGenerator(GENERATOR_NAME);

        // Check the IDs: the one from newGenerator should be larger.
        System.out.println("oldGenerator.newId = " + oldGenerator.newId());
        System.out.println("newGenerator.newId = " + newGenerator.newId());

        Hazelcast.shutdownAll();
    }
}
