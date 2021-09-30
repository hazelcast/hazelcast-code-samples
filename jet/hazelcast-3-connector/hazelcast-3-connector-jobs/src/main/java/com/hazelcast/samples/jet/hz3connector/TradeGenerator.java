package com.hazelcast.samples.jet.hz3connector;

import com.hazelcast.jet.accumulator.LongLongAccumulator;
import com.hazelcast.jet.pipeline.SourceBuilder.TimestampedSourceBuffer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toMap;

final class TradeGenerator {

    private static final int LOT = 100;
    private static final long MONEY_SCALE_FACTOR = 1000L;

    private final List<String> tickers;
    private final long emitPeriodNanos;
    private final long startTimeMillis;
    private final long startTimeNanos;
    private final long maxLagNanos;
    private final Map<String, LongLongAccumulator> pricesAndTrends;

    private long scheduledTimeNanos;

    TradeGenerator(int tradesPerSec, int maxLagMillis) {
        this.tickers = Arrays.asList("AAPL", "GOOGL", "AMZN", "FB", "NFLX");
        this.maxLagNanos = MILLISECONDS.toNanos(maxLagMillis);
        this.pricesAndTrends = tickers.stream()
                                      .collect(toMap(t -> t, t -> new LongLongAccumulator(50 * MONEY_SCALE_FACTOR,
                                              MONEY_SCALE_FACTOR / 10)));
        this.emitPeriodNanos = SECONDS.toNanos(1) / tradesPerSec;
        this.startTimeNanos = System.nanoTime();
        this.scheduledTimeNanos = startTimeNanos;
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void generateTrades(TimestampedSourceBuffer<Trade> buf) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long nowNanos = System.nanoTime();
        while (scheduledTimeNanos <= nowNanos) {
            String ticker = tickers.get(rnd.nextInt(tickers.size()));
            LongLongAccumulator priceAndDelta = pricesAndTrends.get(ticker);
            long price = getNextPrice(priceAndDelta, rnd) / MONEY_SCALE_FACTOR;
            long tradeTimeNanos = scheduledTimeNanos - (maxLagNanos > 0 ? rnd.nextLong(maxLagNanos) : 0L);
            long tradeTimeMillis = startTimeMillis + NANOSECONDS.toMillis(tradeTimeNanos - startTimeNanos);
            Trade trade = new Trade(tradeTimeMillis, ticker, rnd.nextInt(1, 10) * LOT, price);
            buf.add(trade, tradeTimeMillis);
            scheduledTimeNanos += emitPeriodNanos;
            if (scheduledTimeNanos > nowNanos) {
                // Refresh current time before checking against scheduled time
                nowNanos = System.nanoTime();
            }
        }
    }

    private static long getNextPrice(LongLongAccumulator priceAndDelta, ThreadLocalRandom rnd) {
        long price = priceAndDelta.get1();
        long delta = priceAndDelta.get2();
        if (price + delta <= 0) {
            //having a negative price doesn't make sense for most financial instruments
            delta = -delta;
        }
        price = price + delta;
        delta = delta + rnd.nextLong(MONEY_SCALE_FACTOR + 1) - MONEY_SCALE_FACTOR / 2;

        priceAndDelta.set1(price);
        priceAndDelta.set2(delta);

        return price;
    }

}
