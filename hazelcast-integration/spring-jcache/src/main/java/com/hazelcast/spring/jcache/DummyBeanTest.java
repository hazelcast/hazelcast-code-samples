package com.hazelcast.spring.jcache;

import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

/**
 * Calculation time for non-cached and cached results of DummyBean.getCity() invocations
 */
class DummyBeanTest {

    static void doInvocation(ApplicationContext context) {
        IDummyBean dummy = (IDummyBean) context.getBean("dummyBean");

        String logFormat = "%s call took %d millis with result: %s";
        long start1 = nanoTime();
        String city = dummy.getCity();
        long end1 = nanoTime();
        out.println(format(logFormat, "First", TimeUnit.NANOSECONDS.toMillis(end1 - start1), city));

        long start2 = nanoTime();
        city = dummy.getCity();
        long end2 = nanoTime();
        out.println(format(logFormat, "Second", TimeUnit.NANOSECONDS.toMillis(end2 - start2), city));
    }
}
