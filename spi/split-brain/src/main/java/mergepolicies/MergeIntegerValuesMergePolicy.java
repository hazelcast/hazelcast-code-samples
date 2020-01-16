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

import com.hazelcast.cardinality.CardinalityEstimator;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.merge.MergingValue;
import com.hazelcast.spi.merge.SplitBrainMergePolicy;

/**
 * Simple merge policy, which only requires {@link MergingValue}.
 * <p>
 * This merge policy will work on all split-brain capable data structures
 * (except {@link CardinalityEstimator}, which doesn't allow custom merge policies).
 * <p>
 * For the {@link com.hazelcast.collection.ISet}, {@link com.hazelcast.collection.IList},
 * {@link com.hazelcast.collection.IQueue} and {@link com.hazelcast.ringbuffer.Ringbuffer},
 * this policy will return {@code null} since these structures will provide the entire
 * collection to the merge policy instead of individual items and the type of the
 * collection is never an integer.
 *
 * @param <V> the (deserialized) type of the merging value
 * @see com.hazelcast.spi.merge.SplitBrainMergeTypes
 */
public class MergeIntegerValuesMergePolicy<V> implements SplitBrainMergePolicy<V, MergingValue<V>, Object> {

    @Override
    public Object merge(MergingValue<V> mergingValue, MergingValue<V> existingValue) {
        // the in-memory format of the data structure maybe BINARY, but since we need to compare
        // the real value, we have to use getValue() instead of getRawValue()
        Object mergingUserValue = mergingValue.getValue();
        Object existingUserValue = existingValue == null ? null : existingValue.getValue();
        System.out.println("========================== Merging..."
                + "\n    mergingValue: " + mergingUserValue
                + "\n    existingValue: " + existingUserValue
                + "\n    mergingValue class: " + mergingUserValue.getClass().getName()
                + "\n    existingValue class: " + (existingUserValue == null ? "null" : existingUserValue.getClass().getName())
        );
        if (mergingUserValue instanceof Integer) {
            return mergingValue.getRawValue();
        }
        return null;
    }

    @Override
    public void writeData(ObjectDataOutput out) {
    }

    @Override
    public void readData(ObjectDataInput in) {
    }
}
