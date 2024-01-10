package usercodenamespaces;

import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class IncrementingEntryProcessor implements EntryProcessor<Integer, Integer, Object> {

    private static final long serialVersionUID = 1694797399632443061L;

    @Override
    public Object process(Map.Entry<Integer, Integer> entry) {
        Integer origValue = entry.getValue();
        Integer newValue = origValue + 1;
        entry.setValue(newValue);

        return newValue;
    }
}
