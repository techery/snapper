package io.techery.snapper.executor;

import java.util.concurrent.ExecutorService;

public class FixedExecutorFactory implements io.techery.snapper.executor.ExecutorFactory {

    private ExecutorService executorService;

    public FixedExecutorFactory(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public ExecutorService createExecutor() {
        return executorService;
    }
}
