package com.hazelcast.samples.jet.hz3connector;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class Trade implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long time;
    private final String ticker;
    private final long quantity;
    private final long price;

    Trade(long time, @Nonnull String ticker, long quantity, long price) {
        this.time = time;
        this.ticker = Objects.requireNonNull(ticker);
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Event time of the trade.
     */
    public long getTime() {
        return time;
    }

    /**
     * Name of the instrument being traded.
     */
    @Nonnull
    public String getTicker() {
        return ticker;
    }

    /**
     * Quantity of the trade, the amount of the instrument that has been
     * traded.
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Price at which the transaction took place.
     */
    public long getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Trade{time=" + time + ", ticker='" + ticker + '\'' + ", quantity=" + quantity
                + ", price=" + price + '}';
    }
}
