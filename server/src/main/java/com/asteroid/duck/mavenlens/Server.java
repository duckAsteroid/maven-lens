/**
 * Copyright (c) 2017 Dr. Chris Senior
 */
package com.asteroid.duck.mavenlens;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class Server {
    private final ReentrantLock lock = new ReentrantLock(false);
    private final PathRules rules;
    private final int port;
    private final int pathPrefix;

    private boolean running = false;
    private Undertow server;

    public Server(int port, PathRules rules, String prefix) {
        this(port, rules, prefix != null ? prefix.length() : 0);
    }

    public Server(int port, PathRules rules, int prefixLength) {
        this.port = port;
        this.rules = rules;
        this.pathPrefix = prefixLength;
    }


    public Server start() {
        lock.lock();
        try {
            if (!running) {
                running = true;
                server = Undertow.builder()
                        .addHttpListener(port, "localhost")
                        .setHandler(new HttpHandler() {
                            @Override
                            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                                handleHttpRequest(exchange);
                            }
                        }).build();
                server.start();
            }
            return this;
        } finally {
            lock.unlock();
        }
    }

    private void handleHttpRequest(final HttpServerExchange exchange) throws Exception {
        String rawRequestPath = exchange.getRequestPath();
        String requestPath = rawRequestPath.substring(pathPrefix);
        if (rules == null || rules.isAllowed(requestPath)) {
            InputStream content = rules.getContent(requestPath);
            if (content != null) {
                System.out.println("OK: " + requestPath);
                accept(exchange, content);
            }
            else {
                reject(exchange, requestPath);
            }
        }
        else {
            reject(exchange, requestPath);
        }

        // stop??
        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        if (queryParameters.containsKey("stop") && queryParameters.get("stop").contains("true")) {
            this.stop();
        }
    }

    private void accept(final HttpServerExchange exchange, final InputStream read) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytes;
        StreamSinkChannel responseChannel = exchange.getResponseChannel();
        do {
            bytes = read.read(buffer.array());
            if (bytes > 0) {
                buffer.limit(bytes);
                responseChannel.write(buffer);
            }
        }
        while(bytes > 0);
        responseChannel.flush();
        responseChannel.close();
    }

    private void reject(final HttpServerExchange exchange, final String requestPath) {
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Rejected: "+requestPath);
        System.err.println("REJECT: "+requestPath);
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() throws InterruptedException {
        lock.lock();
        try {
            if(running) {
                running = false;
                if (server != null) {
                    server.stop();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server s = new Server(9076, new PathRules(), null);
        s.start();
    }
}
