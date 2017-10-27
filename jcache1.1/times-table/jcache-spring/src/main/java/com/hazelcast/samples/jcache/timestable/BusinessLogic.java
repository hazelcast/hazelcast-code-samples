package com.hazelcast.samples.jcache.timestable;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>The business logic of the application,
 * doing mathematics.
 * </p>
 *
 * TODO: Add recursive @CacheResult
 */
@Component
@Slf4j
public class BusinessLogic {

    /**
     * <p>Find the product of two numbers. Although
     * obviously we can multiply them together in
     * all cases, we're going to try to avoid that
     * and use the distribute property of the times
     * table.
     * </p>
     * <p>The annotation {@code @CacheResult} stores
     * the result so it's not calculated a second
     * time.
     * </p>
     *
     * @param tuple "{@code (x, y)}"
     * @return "{@code x * y}"
     */
    public int product(Tuple tuple) {
            log.info("product({})", tuple);

        int operand1 = tuple.getOperand1();
        int operand2 = tuple.getOperand2();

        if (operand1 == 1) {
            // 1 * y == y
            return operand2;
        } else {
            // (1 + x) * y = y + (x * y)
            return operand2 + this.product(new Tuple(operand1 - 1, operand2));
        }
    }

}
