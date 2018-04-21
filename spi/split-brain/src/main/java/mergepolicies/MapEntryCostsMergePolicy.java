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

package mergepolicies;

import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.merge.MergingCosts;
import com.hazelcast.spi.merge.SplitBrainMergePolicy;
import com.hazelcast.spi.merge.SplitBrainMergeTypes.MapMergeTypes;

/**
 * Merge policy which requires an {@link IMap} data structure.
 * <p>
 * Even if other data structures would provide the underlying {@link MergingCosts},
 * this merge policy will just work when configured with an {@link IMap},
 * due to the required {@link MapMergeTypes}.
 *
 * @see com.hazelcast.spi.merge.SplitBrainMergeTypes
 */
public class MapEntryCostsMergePolicy implements SplitBrainMergePolicy<Data, MapMergeTypes> {

    @Override
    public Data merge(MapMergeTypes mergingValue, MapMergeTypes existingValue) {
        if (existingValue == null) {
            return mergingValue.getValue();
        }
        System.out.println("========================== Merging key " + mergingValue.getDeserializedKey() + "..."
                + "\n    mergingValue costs: " + mergingValue.getCost()
                + "\n    existingValue costs: " + existingValue.getCost()
        );
        // the merging value wins, if it's costs are higher
        if (mergingValue.getCost() > existingValue.getCost()) {
            return mergingValue.getValue();
        }
        return existingValue.getValue();
    }

    @Override
    public void writeData(ObjectDataOutput out) {
    }

    @Override
    public void readData(ObjectDataInput in) {
    }
}
