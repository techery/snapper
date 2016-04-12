package io.techery.snapper.datacollection;

import java.util.concurrent.Executor;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;
import io.techery.snapper.executor.ExecutorFactory;

public class DefaultDataCollectionFactory implements DataCollectionFactory {

    private final StorageFactory storageFactory;
    private final ExecutorFactory executorFactory;

    public DefaultDataCollectionFactory(StorageFactory storageFactory, ExecutorFactory executorFactory) {
        this.storageFactory = storageFactory;
        this.executorFactory = executorFactory;
    }

    @Override
    public <T extends Indexable> DataCollection<T> createCollection(Class<T> clazz, String name) throws Throwable {
        Storage<T> storage = storageFactory.createStorage(clazz, name);
        Executor executor = executorFactory.createExecutor();
        return new DataCollection<T>(storage, executor);
    }

}
