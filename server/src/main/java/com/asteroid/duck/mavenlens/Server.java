/**
 * Copyright (c) 2017 Dr. Chris Senior
 */
package com.asteroid.duck.mavenlens;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

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
    private boolean running = false;
    private final int port;
    private Undertow server;

    public Server(int port) {
        this.port = port;
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
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World");
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
        Server s = new Server(9076);
        s.start();
        Thread.sleep(15000);
        s.stop();
    }
}
