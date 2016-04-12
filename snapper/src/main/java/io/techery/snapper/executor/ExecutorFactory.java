package io.techery.snapper.executor;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {
    ExecutorService createExecutor();
}
