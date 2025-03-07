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

import com.hazelcast.function.ConsumerEx;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.internal.util.Preconditions;
import com.hazelcast.jet.contrib.http.impl.HttpListenerSinkContext;
import com.hazelcast.jet.contrib.http.impl.HttpListenerSinkContext.SinkType;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.core.ProcessorSupplier;
import com.hazelcast.jet.core.processor.SinkProcessors;
import com.hazelcast.jet.impl.pipeline.SinkImpl;
import com.hazelcast.jet.pipeline.Sink;

import javax.net.ssl.SSLContext;
import java.util.Objects;

import static com.hazelcast.jet.contrib.http.impl.HttpListenerSinkContext.SinkType.SSE;
import static com.hazelcast.jet.contrib.http.impl.HttpListenerSinkContext.SinkType.WEBSOCKET;
import static com.hazelcast.jet.core.ProcessorMetaSupplier.forceTotalParallelismOne;
import static com.hazelcast.jet.impl.pipeline.SinkImpl.Type.TOTAL_PARALLELISM_ONE;

/**
 * See {@link HttpListenerSinks#builder()}.
 *
 * @param <T> the type of the pipeline item.
 */
public class HttpListenerSinkBuilder<T> {

    /**
     * Default host for HTTP(s) listener
     */
    public static final String DEFAULT_HOST = "0.0.0.0";

    /**
     * Default port for HTTP Listener sink
     */
    public static final int DEFAULT_PORT = 8081;

    /**
     * Default path for HTTP Listener sink
     */
    public static final String DEFAULT_PATH = "/";

    private static final int PORT_MAX = 0xFFFF;

    private int port = DEFAULT_PORT;
    private String path = DEFAULT_PATH;
    private boolean mutualAuthentication;
    private int accumulateLimit;
    private SupplierEx<String> hostFn;
    private SupplierEx<SSLContext> sslContextFn;
    private FunctionEx<T, String> toStringFn = Object::toString;

    HttpListenerSinkBuilder() {
    }

    /**
     * Set the function which provides the host name. The function will be
     * called for each member and it should return a matching interface.
     * <p>
     * For example to pick the first available non-loopback interface :
     * <pre>{@code
     * builder.hostFn(() -> {
     *     Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
     *     while (networkInterfaces.hasMoreElements()) {
     *         NetworkInterface networkInterface = networkInterfaces.nextElement();
     *         Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
     *         while (inetAddresses.hasMoreElements()) {
     *             InetAddress inetAddress = inetAddresses.nextElement();
     *             if (inetAddress.isLoopbackAddress()) {
     *                 continue;
     *             }
     *             return inetAddress.getHostName();
     *         }
     *     }
     *     throw new IllegalStateException("No available network interface");
     * })
     * }</pre>
     * <p>
     * Default value is to bind to all interfaces {@link #DEFAULT_HOST}.
     *
     * @param hostFn the function which provides the host name.
     */
    
    public HttpListenerSinkBuilder<T> hostFn( SupplierEx<String> hostFn) {
        this.hostFn = hostFn;
        return this;
    }

    /**
     * Set the port which the sink binds and listens.
     * <p>
     * For example to bind to port `5902`:
     * <pre>{@code
     * builder.port(5902)
     * }</pre>
     * <p>
     * Default value is {@link #DEFAULT_PORT} {@code 8081}.
     *
     * @param port the port which the source binds and listens.
     */
    
    public HttpListenerSinkBuilder<T> port(int port) {
        if (port < 0 || port > PORT_MAX) {
            throw new IllegalArgumentException("Port out of range: " + port + ". Allowed range [0,65535]");
        }
        this.port = port;
        return this;
    }

    /**
     * Set the SSL Context function which will be used to initialize underlying
     * HTTPs listener for secure connections.
     * <p>
     * For example:
     * <pre>{@code
     * builder.sslContextFn(() -> {
     *     SSLContext context = SSLContext.getInstance("TLS");
     *     KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
     *     KeyStore ks = KeyStore.getInstance("PKCS12");
     *     char[] password = "123456".toCharArray();
     *     ks.load(new FileInputStream("the.keystore"), password);
     *     kmf.init(ks, password);
     *     TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
     *     KeyStore ts = KeyStore.getInstance("PKCS12");
     *     ts.load(new FileInputStream("the.truststore"), password);
     *     tmf.init(ts);
     *     context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
     *     return context;
     * })
     * }</pre>
     * <p>
     * Default value is {@code null}.
     *
     * @param sslContextFn the function to create {@link SSLContext} which used
     *                     to initialize underlying HTTPs listener for secure
     *                     connections.
     */
    
    public HttpListenerSinkBuilder<T> sslContextFn( SupplierEx<SSLContext> sslContextFn) {
        this.sslContextFn = Objects.requireNonNull(sslContextFn);
        return this;
    }

    /**
     * Set that sink should authenticate the connected clients. This
     * parameter is ignored if {@link #sslContextFn(SupplierEx)} is not set.
     * <p>
     * Default values is {@code false}, sink does not authenticate connected
     * clients.
     */
    
    public HttpListenerSinkBuilder<T> enableMutualAuthentication() {
        this.mutualAuthentication = true;
        return this;
    }

    /**
     * Set the path which server accepts connections.
     * <p>
     * For example:
     * <pre>{@code
     * builder.path("/user")
     * }</pre>
     * <p>
     * Default value is {@code /}.
     *
     * @param path the path which server accepts connections
     */
    
    public HttpListenerSinkBuilder<T> path( String path) {
        this.path = Objects.requireNonNull(path);
        return this;
    }

    /**
     * Set that sink should accumulate items up to the  given limit if there is
     * no connected client and send them when a client connects. After reaching
     * the limit sink drops the items.
     * <p>
     * Default value is {@code 0} meaning sink drops the items if there is no
     * connected client.
     *
     * @param accumulateLimit the size of the buffer for the accumulated
     *                        messages.
     */
    
    public HttpListenerSinkBuilder<T> accumulateItems(int accumulateLimit) {
        Preconditions.checkPositive(accumulateLimit, "accumulateLimit should be a positive value");
        this.accumulateLimit = accumulateLimit;
        return this;
    }

    /**
     * Set the function which converts each item to a string.
     * <p>
     * By default source converts each item to string using
     * {@link Object#toString()}.
     *
     * @param toStringFn the function which converts each item to a string.
     */
    
    public HttpListenerSinkBuilder<T> toStringFn( FunctionEx<T, String> toStringFn) {
        this.toStringFn = Objects.requireNonNull(toStringFn);
        return this;
    }

    /**
     * Build a Websocket {@link Sink} with supplied parameters.
     */
    
    public Sink<T> buildWebsocket() {
        return build(path, port, accumulateLimit, mutualAuthentication, WEBSOCKET, sslContextFn, hostFn(), toStringFn);
    }

    /**
     * Build a Server Sent Events {@link Sink} with supplied parameters.
     */
    
    public Sink<T> buildServerSent() {
        return build(path, port, accumulateLimit, mutualAuthentication, SSE, sslContextFn, hostFn(), toStringFn);
    }

    private Sink<T> build(
             String path,
            int port,
            int accumulateLimit,
            boolean mutualAuthentication,
             SinkType sinkType,
             SupplierEx<SSLContext> sslContextFn,
             SupplierEx<String> hostFn,
             FunctionEx<T, String> toStringFn
    ) {
        SupplierEx<Processor> supplier =
                SinkProcessors.writeBufferedP(ctx -> new HttpListenerSinkContext<>(ctx, path, port,
                                accumulateLimit, mutualAuthentication, sinkType, sslContextFn, hostFn, toStringFn),
                        HttpListenerSinkContext::receive,
                        ConsumerEx.noop(),
                        HttpListenerSinkContext::close
                );
        String sinkName = sinkType == WEBSOCKET ? websocketName() : serverSentName();
        return new SinkImpl<>(sinkName,
                forceTotalParallelismOne(ProcessorSupplier.of(supplier), sinkName), TOTAL_PARALLELISM_ONE);
    }

    private String websocketName() {
        return "websocket:" + port;
    }

    private String serverSentName() {
        return "serverSent:" + port;
    }

    private SupplierEx<String> hostFn() {
        if (hostFn != null) {
            return hostFn;
        }
        return () -> DEFAULT_HOST;
    }

}
