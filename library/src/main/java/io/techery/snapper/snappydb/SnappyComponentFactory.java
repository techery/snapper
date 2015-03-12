package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.storage.DatabaseAdapter;

public class SnappyComponentFactory extends ComponentFactory {

    private final SnappyDBFactory snappyDBFactory;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SnappyComponentFactory(SnappyDBFactory snappyDBFactory) {
        this.snappyDBFactory = snappyDBFactory;
    }

    @Override
    public DatabaseAdapter createDatabase(String simpleName) throws IOException {
        return snappyDBFactory.createDatabase(simpleName);
    }

    @Override
    public ExecutorService createExecutor() {
        return executorService;
    }
}
