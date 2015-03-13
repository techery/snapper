package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.Executor;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.PersistentStorage;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;

public class SnappyStorageFactory implements StorageFactory {
    private final ComponentFactory componentFactory;

    public SnappyStorageFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    public <T extends Indexable> Storage<T> createStorage(Class<T> className) throws IOException {
        DatabaseAdapter databaseAdapter = this.componentFactory.createDatabase(className.getSimpleName());
        ObjectConverter<T> objectConverter = this.componentFactory.createConverter(className);
        Executor executor = this.componentFactory.createStorageExecutor();

        return new PersistentStorage<>(databaseAdapter, objectConverter, executor);
    }
}
