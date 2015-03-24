package io.techery.snapper;

import java.util.concurrent.ExecutorService;

import io.techery.snapper.util.SimpleExecutorService;

public class BaseSyncTestCase extends BaseTestCase {

    private ExecutorService createSimpleExecutor() {
        return new SimpleExecutorService() {
            @Override public void execute(Runnable command) {
                command.run();
            }
        };
    }

    @Override protected ExecutorService provideStorageExecutor() {
        return createSimpleExecutor();
    }

    @Override protected ExecutorService provideCollectionExecutor() {
        return createSimpleExecutor();
    }
}
