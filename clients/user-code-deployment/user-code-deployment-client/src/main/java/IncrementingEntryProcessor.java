import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class IncrementingEntryProcessor implements EntryProcessor<Integer, Integer, Object> {

    @Override
    public Object process(Map.Entry<Integer, Integer> entry) {
        Integer origValue = entry.getValue();
        Integer newValue = origValue + 1;
        entry.setValue(newValue);

        return newValue;
    }
}
