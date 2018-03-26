package com.hazelcast.samples.jcache.timestable;

import lombok.extern.slf4j.Slf4j;

import javax.cache.Cache;

/**
 * Non-Spring version of the business logic of the application,
 * doing mathematics.
 */
@Slf4j
public class BusinessLogic {

    /**
     * Find the product of two numbers. Although
     * obviously we can multiply them together in
     * all cases, we're going to try to avoid that
     * and use the distribute property of the times
     * table.
     * <p>
     * Implement {@code @CacheResult} without
     * annotations. Look for a stored value. If
     * not found, calculate store and return.
     *
     * @param tuple "{@code (x, y)}"
     * @param cache Where results are stored
     * @return "{@code x * y}"
     */
    public static Integer product(Tuple tuple, Cache<Tuple, Integer> cache) {
        log.info("product({})", tuple);

        Integer cacheResult = cache.get(tuple);

        // Cache hit
        if (cacheResult != null) {
            return cacheResult;
        }

        // Cache miss - calculate
        int operand1 = tuple.getOperand1();
        int operand2 = tuple.getOperand2();

        if (operand1 == 1) {
            // 1 * y == y
            cacheResult = operand2;
        } else {
            // (1 + x) * y = y + (x * y)
            cacheResult = operand2 + BusinessLogic.product(new Tuple(operand1 - 1, operand2), cache);
        }

        // Cache miss - store
        cache.put(tuple, cacheResult);

        return cacheResult;
    }
}
