import com.hazelcast.map.AbstractEntryProcessor;

import java.util.Map;

public class IncrementingEntryProcessor extends AbstractEntryProcessor<Integer, Integer> {

    @Override
    public Object process(Map.Entry<Integer, Integer> entry) {
        Integer origValue = entry.getValue();
        Integer newValue = origValue + 1;
        entry.setValue(newValue);

        return newValue;
    }
}
