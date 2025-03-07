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

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.pipeline.StreamSource;
import io.undertow.Undertow;


import static com.hazelcast.internal.util.Preconditions.checkNotNull;


/**
 * Contains factory methods for creating HTTP(s) listener sources which
 * listens for HTTP(s) requests. There are multiple variants for
 * handling the payloads. For JSON payloads you can use built-in object
 * mapping. You can get raw string payload or provide a
 * {@code mapToItemFn} function to deserialize the payload.
 */
public final class HttpListenerSources {

    private HttpListenerSources() {
    }

    /**
     * Return a builder object which offers a step-by-step fluent API to build
     * a custom HTTP(s) listener {@link StreamSource source} for the Pipeline
     * API.
     * <p>
     * The source creates an {@link Undertow} server at each member which
     * listens the requests from the specified port. If user provides an ssl
     * context, source initializes the listeners with secure connection.
     * <p>
     * If you start more than one Jet instances on the same host machine, the
     * source will fail to bind to the same port thus the job will fail.
     * <p>
     * Source emits items of type {@link String} if {@link HttpListenerSourceBuilder#type(Class)}
     * or {@link HttpListenerSourceBuilder#mapToItemFn(FunctionEx)} is not set.
     */
    public static HttpListenerSourceBuilder<String> builder() {
        return new HttpListenerSourceBuilder<>();
    }

    /**
     * Create a source that listens for HTTP requests from
     * {@link HttpListenerSourceBuilder#DEFAULT_PORT} {@code 8080} and converts the
     * payload to {@code String}.
     * <p>
     * See {@link #builder()}
     */
    
    public static StreamSource<String> httpListener() {
        return builder().build();
    }

    /**
     * Create a source that listens for HTTP requests from given port
     * and maps the payload to specified type. Source expects a JSON formatted
     * payload.
     * <p>
     * See {@link #builder()}
     *
     * @param port The port for HTTP listener to bind. The source will listen
     *             for connections on given port on the same host address with
     *             the member.
     * @param type Class type for the objects to be emitted. Received
     *             JSON payloads will be mapped to objects of specified
     *             type.
     */
    
    public static <T> StreamSource<T> httpListener(int port,  Class<T> type) {
        checkNotNull(type, "type cannot be null");
        return builder().port(port).type(type).build();
    }

    /**
     * Create a source that listens for HTTP requests from given port
     * and maps the payload to pipeline item using specified {@code mapToItemFn}.
     * <p>
     * See {@link #builder()}
     *
     * @param port        The port for HTTP listener to bind. The source will
     *                    listen for connections on given port on the same host
     *                    address with the member.
     * @param mapToItemFn the function which converts the received payload to
     *                    pipeline item.
     */
    
    public static <T> StreamSource<T> httpListener(int port,  FunctionEx<byte[], T> mapToItemFn) {
        checkNotNull(mapToItemFn, "mapToItemFn cannot be null");
        return builder().port(port).mapToItemFn(mapToItemFn).build();
    }

}
