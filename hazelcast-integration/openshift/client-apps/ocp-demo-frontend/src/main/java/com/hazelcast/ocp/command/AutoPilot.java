package com.hazelcast.ocp.command;

import com.hazelcast.core.IMap;
import com.hazelcast.util.Preconditions;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
@Slf4j
class AutoPilot {

    private int readCount;
    private int insertCount;
    private int poolSize;
    private int valueSize;
    private IMap<String, String> map;

    private ExecutorService executorService;
    private List<Callable<Integer>> callables;

    void start() {
        Preconditions.checkNotNull(map);
        Preconditions.checkPositive(poolSize, "invalid pool size");

        initialize();
        log.info("Auto pilot started with {} number of threads", poolSize);

        try {
            executorService.invokeAll(callables)
                    .stream()
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(it -> log.info("Map Size = {}", it));

            executorService.shutdown();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private void initialize() {
        executorService = Executors.newFixedThreadPool(poolSize);

        List<Callable<Integer>> inserts = IntStream.range(0, insertCount)
                .boxed()
                .map(it -> (Callable<Integer>) () -> {
                    String key = RandomStringUtils.randomAlphanumeric(42);
                    String value = RandomStringUtils.randomAlphabetic(valueSize);
                    map.put(key, value);
                    return map.size();
                })
                .collect(Collectors.toList());

        List<Callable<Integer>> reads = IntStream.range(0, readCount).boxed()
                .map(it -> (Callable<Integer>) () -> {
                    String key = RandomStringUtils.randomAlphanumeric(42);
                    String value = map.get(key);
                    log.info("randomly get value : {} size: {}", value, map.size());
                    return map.size();
                })
                .collect(Collectors.toList());

        callables = new ArrayList<>();
        callables.addAll(reads);
        callables.addAll(inserts);

        Collections.shuffle(callables);
    }
}
