package io.techery.snapper.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class SimpleExecutorService extends AbstractExecutorService {

    private volatile boolean isShutDown;

    @Override public synchronized void shutdown() {
        isShutDown = true;
    }

    @Override public synchronized List<Runnable> shutdownNow() {
        isShutDown = true;
        return Collections.emptyList();
    }

    @Override public synchronized boolean isShutdown() {
        return isShutDown;
    }

    @Override public synchronized boolean isTerminated() {
        return isShutDown;
    }

    @Override public synchronized boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return isShutDown;
    }
}
