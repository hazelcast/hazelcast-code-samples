/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

import java.io.Serializable;
import java.util.Objects;

public class Trade implements Serializable {

    private String id;
    private long timestamp;
    private String symbol;
    private int quantity;
    private int price; // in cents

    public Trade() {
    }

    /**
     * Timestamp for the trade in UNIX timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * The symbol
     */
    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * The price in cents
     */
    public int getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s %d %s %d %d", id, timestamp, symbol, quantity, price);
    }


    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Trade trade = (Trade) o;
        return timestamp == trade.timestamp &&
                quantity == trade.quantity &&
                price == trade.price &&
                Objects.equals(id, trade.id) &&
                Objects.equals(symbol, trade.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, symbol, quantity, price);
    }

}