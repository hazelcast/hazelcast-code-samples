package hazelcast.platform.labs.retail;

import com.hazelcast.map.EntryProcessor;
import hazelcast.platform.labs.retail.domain.Inventory;

import java.util.Map;

/*
 * Safely decrements the inventory level for an item without the need for transactions
 * Example usage below (runs on the client)
 *
 *  int quantitySold = 5;
 *  int itemNumber = 99;
 *  int quantityRemaining = inventoryMap.executeOnKey(itemNumber, new InventoryUpdateEntryProcessor(quantitySold));
 */
public class InventoryUpdateEntryProcessor implements EntryProcessor<Integer, Inventory, Integer> {
    
    // we need a default ctor for some methods of serialization
    public InventoryUpdateEntryProcessor(){}

    public InventoryUpdateEntryProcessor(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    private int quantitySold;

    @Override
    public Integer process(Map.Entry<Integer, Inventory> entry) {
        Inventory inventory = entry.getValue();
        if (inventory == null) {
            // this can happen on the backup
            return 0;
        }
        int quantityInStock = inventory.getQuantityInStock();
        if (quantityInStock < quantitySold){
            throw new RuntimeException("Insufficient quantity available to fill order.");
        }

        quantityInStock -= quantitySold;
        inventory.setQuantityInStock(quantityInStock);
        entry.setValue(inventory);
        return quantityInStock;
    }

    @Override
    public EntryProcessor<Integer, Inventory, Integer> getBackupProcessor() {
        return this;
    }
}
