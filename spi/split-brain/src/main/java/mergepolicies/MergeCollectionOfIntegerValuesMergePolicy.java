package mergepolicies;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.merge.MergingValue;
import com.hazelcast.spi.merge.SplitBrainMergePolicy;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Merges only {@link Integer} values of the given collections.
 * <p>
 * This policy is applicable only for {@link com.hazelcast.collection.ISet},
 * {@link com.hazelcast.collection.IQueue} and {@link com.hazelcast.collection.IList}.
 */
public class MergeCollectionOfIntegerValuesMergePolicy
        implements SplitBrainMergePolicy<Collection<Object>, MergingValue<Collection<Object>>, Collection<Object>> {

    @Override
    public Collection<Object> merge(MergingValue<Collection<Object>> mergingValue,
                                    MergingValue<Collection<Object>> existingValue) {
        Collection<Object> result = new ArrayList<>();
        for (Object value : mergingValue.getValue()) {
            if (value instanceof Integer) {
                result.add(value);
            }
        }
        if (existingValue != null) {
            for (Object value : existingValue.getValue()) {
                if (value instanceof Integer) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    @Override
    public void writeData(ObjectDataOutput out) {
    }

    @Override
    public void readData(ObjectDataInput in) {
    }
}
