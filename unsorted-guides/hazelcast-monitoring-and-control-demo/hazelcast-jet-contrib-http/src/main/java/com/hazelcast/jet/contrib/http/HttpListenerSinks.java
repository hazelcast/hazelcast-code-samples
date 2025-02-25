/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.contrib.http;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.contrib.http.impl.HttpListenerSinkContext;
import com.hazelcast.jet.impl.util.ExceptionUtil;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.ringbuffer.ReadResultSet;
import com.hazelcast.ringbuffer.Ringbuffer;
import io.undertow.Undertow;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Contains factory methods for creating WebSocket and Server-Sent
 * Events sinks. Clients can connect to the sinks and stream the
 * results of the pipeline.
 * <p>
 * Server addresses can be retrieved from
 * {@link #sinkAddress(HazelcastInstance, Job)}.
 */
public final class HttpListenerSinks {

    private static final int ADDRESS_RETRIEVE_TIMEOUT_MS = 10_000;

    private HttpListenerSinks() {
    }

    /**
     * Return a builder object which offers a step-by-step fluent API to build
     * a custom HTTP(s) Listener {@link Sink sink} for the Pipeline API.
     * <p>
     * The sink is not distributed, it creates an {@link Undertow} server at
     * one of the members and send items to the connected clients at specified
     * port. If user provides an ssl context, clients should connect with a
     * secure connection.
     */
    
    public static <T> HttpListenerSinkBuilder<T> builder() {
        return new HttpListenerSinkBuilder<>();
    }

    /**
     * Create a Websocket sink which sends items to the connected clients at
     * {@link HttpListenerSinkBuilder#DEFAULT_PORT} and {@link HttpListenerSinkBuilder#DEFAULT_PATH}
     * by converting each item to string using {@link Object#toString()}. Sink
     * does not use secure connections and does not accumulate items if there
     * is no connected client.
     * <p>
     * See {@link #builder()}
     */
    
    public static <T> Sink<T> websocket() {
        return HttpListenerSinks.<T>builder().buildWebsocket();
    }

    /**
     * Create a Websocket sink which sends items to the connected clients at
     * specified {@code port} and {@code path} by converting each item to
     * string using {@link Object#toString()}. Sink does not use secure
     * connections and does not accumulate items if there is no connected
     * client.
     * <p>
     * See {@link #builder()}
     *
     * @param path the path which websocket server accepts connections
     * @param port the port which websocket server to bind.
     */
    
    public static <T> Sink<T> websocket( String path, int port) {
        return HttpListenerSinks.<T>builder().path(path).port(port).buildWebsocket();
    }

    /**
     * Create a Server-sent Event sink which sends items to the connected
     * clients at {@link HttpListenerSinkBuilder#DEFAULT_PORT} and
     * {@link HttpListenerSinkBuilder#DEFAULT_PATH} by converting each item to string
     * using {@link Object#toString()}. Sink does not use secure connections
     * and does not accumulate items if there is no connected client.
     * <p>
     * See {@link #builder()}
     */
    
    public static <T> Sink<T> sse() {
        return HttpListenerSinks.<T>builder().buildServerSent();
    }

    /**
     * Create a Server-sent Event sink which sends items to the connected
     * clients at specified {@code port} and {@code path} by converting each
     * item to string using {@link Object#toString()}. Sink does not use secure
     * connections and does not accumulate items if there is no connected
     * client.
     * <p>
     * See {@link #builder()}
     *
     * @param path the path which Server-sent Event server accepts connections
     * @param port the port which Server-sent Event server to bind.
     */
    
    public static <T> Sink<T> sse( String path, int port) {
        return HttpListenerSinks.<T>builder().path(path).port(port).buildServerSent();
    }

    /**
     * Http Listener Sink is not distributed, it creates a listener server on
     * one of the members. The address of this server is stored using the job
     * identifier.
     * <p>
     * Returns the address of the Http Listener Sink server for the given job.
     *
     * @param hz the Hazelcast instance, either client or member
     * @param job the job which has the Http Listener Sink
     */
    public static String sinkAddress(HazelcastInstance hz, Job job) {
        String observableName = HttpListenerSinkContext.getObservableNameByJobId(job.getId());
        Ringbuffer<String> ringBuffer = hz.getRingbuffer(observableName);
        CompletionStage<ReadResultSet<String>> stage = ringBuffer.readManyAsync(0, 1, 1, null);
        try {
            ReadResultSet<String> resultSet = stage.toCompletableFuture()
                    .get(ADDRESS_RETRIEVE_TIMEOUT_MS, MILLISECONDS);
            return resultSet.get(0);
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
}
