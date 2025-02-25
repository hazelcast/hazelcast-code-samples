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

package com.hazelcast.jet.contrib.http.impl;

import com.hazelcast.com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.internal.util.ExceptionUtil;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.pipeline.SourceBuilder.SourceBuffer;
import com.hazelcast.logging.ILogger;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.AllowedMethodsHandler;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.util.StatusCodes;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static io.undertow.Handlers.exceptionHandler;
import static io.undertow.Handlers.path;
import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static io.undertow.util.Methods.POST;
import static io.undertow.util.Methods.PUT;
import static org.xnio.Options.SSL_CLIENT_AUTH_MODE;

public class HttpListenerSourceContext<T> {

    private final ILogger logger;
    private final BlockingQueue<T> queue = new ArrayBlockingQueue<>(1024);
    private final List<T> buffer = new ArrayList<>(1024);
    private final Undertow undertow;
    private final FunctionEx<byte[], T> mapToItemFn;

    public HttpListenerSourceContext(
             Processor.Context context,
            int port,
            boolean mutualAuthentication,
             SupplierEx<String> hostFn,
            SupplierEx<SSLContext> sslContextFn,
             FunctionEx<byte[], T> mapToItemFn
    ) {
        this.logger = context.logger();
        this.mapToItemFn = mapToItemFn;
        String host = hostFn.get();
        Undertow.Builder builder = Undertow.builder();
        if (sslContextFn != null) {
            if (mutualAuthentication) {
                builder.setServerOption(SSL_CLIENT_AUTH_MODE, SslClientAuthMode.REQUIRED);
            }
            builder.addHttpsListener(port, host, sslContextFn.get());
            logger.info("Starting to listen HTTPS messages on https://" + host + ":" + port);
        } else {
            builder.addHttpListener(port, host);
            logger.info("Starting to listen HTTP messages on http://" + host + ":" + port);
        }

        undertow = builder.setServerOption(ENABLE_HTTP2, true).setHandler(handler()).build();
        undertow.start();
    }

    public void fillBuffer(SourceBuffer<T> sourceBuffer) {
        queue.drainTo(buffer);
        buffer.forEach(sourceBuffer::add);
        buffer.clear();
    }

    public void close() {
        undertow.stop();
    }

    private AllowedMethodsHandler handler() {
        return new AllowedMethodsHandler(path().addExactPath("/", exceptionHandler(this::handleMainPath)
                .addExceptionHandler(JsonProcessingException.class, this::handleJsonException)), POST, PUT);
    }

    private void handleMainPath(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes(this::consumeMessage, this::consumeException);
    }

    private void handleJsonException(HttpServerExchange exchange) {
        logger.warning("Supplied payload is not a valid JSON: " +
                exchange.getAttachment(ExceptionHandler.THROWABLE).getMessage());
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
    }

    private void consumeMessage(HttpServerExchange exchange, byte[] message) {
        try {
            queue.put(mapToItemFn.apply(message));
        } catch (InterruptedException e) {
            throw ExceptionUtil.rethrow(e);
        } finally {
            exchange.endExchange();
        }
    }

    private void consumeException(HttpServerExchange ignored, IOException exception) {
        throw ExceptionUtil.sneakyThrow(exception);
    }
}
