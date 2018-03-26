package com.hazelcast.samples.jcache.timestable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.annotation.CacheResult;

/**
 * Spring version of the business logic of the application,
 * doing mathematics.
 */
@Component
@Slf4j
public class BusinessLogic {

    @Autowired
    private BusinessLogic businessLogic;

    /**
     * Find the product of two numbers. Although
     * obviously we can multiply them together in
     * all cases, we're going to try to avoid that
     * and use the distribute property of the times
     * table.
     * <p>
     * The annotation {@code @CacheResult} stores
     * the result so it's not calculated a second
     * time.
     *
     * @param tuple "{@code (x, y)}"
     * @return "{@code x * y}"
     */
    @CacheResult(cacheName = CLI.TIMESTABLE_CACHE_NAME)
    public Integer product(Tuple tuple) {
        log.info("product({})", tuple);

        int operand1 = tuple.getOperand1();
        int operand2 = tuple.getOperand2();

        if (operand1 == 1) {
            // 1 * y == y
            return operand2;
        } else {
            // (1 + x) * y = y + (x * y)
            return operand2 + this.businessLogic.product(new Tuple(operand1 - 1, operand2));
        }
    }
}
