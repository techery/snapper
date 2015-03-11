package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.PersistentStorage;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;

public class SnapperStorageFactory implements StorageFactory {
    private final SnappyComponentFactory componentFactory;

    public SnapperStorageFactory(SnappyComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    public <T extends Indexable> Storage<T> createStorage(Class<T> className) throws IOException {
        DatabaseAdapter databaseAdapter = this.componentFactory.createDatabase(className.getSimpleName());
        ObjectConverter<T> objectConverter = this.componentFactory.createConverter(className);
        ExecutorService executor = this.componentFactory.createExecutor();

        return new PersistentStorage<>(databaseAdapter, objectConverter, executor);
    }
}
