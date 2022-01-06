package example;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.serialization.GenericRecord;

import java.io.Serializable;
import java.util.Map;

public class AddQuantityEntryProcessor implements Serializable, EntryProcessor {

    private String productName;
    private int additionalQuantity;

    public AddQuantityEntryProcessor() {
    }

    public AddQuantityEntryProcessor(String name, int quantity) {
        productName = name;
        additionalQuantity = quantity;
    }

    @Override
    public Object process(Map.Entry entry) {
        GenericRecord genericRecord = (GenericRecord) entry.getValue();
        String name = genericRecord.getString("name");
        if (!name.equals(productName)) {
            return null;
        }

        int quantity = genericRecord.getInt32("quantity");
        quantity += additionalQuantity;

        GenericRecord newRecord = genericRecord.newBuilder()
                .setString("name", name)
                .setInt32("quantity", quantity)
                .build();

        entry.setValue(newRecord);
        return null;
    }
}
