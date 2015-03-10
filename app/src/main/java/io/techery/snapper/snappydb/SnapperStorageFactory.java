package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.PersistentStorage;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;

public class SnapperStorageFactory implements StorageFactory {
    private final ComponentFactory componentFactory;

    public SnapperStorageFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    public <T extends Indexable> Storage<T> createStorage(Class<T> className) throws IOException {

        ExecutorService executor = this.componentFactory.createExecutor();
        DatabaseAdapter databaseAdapter = this.componentFactory.createDatabase(className.getSimpleName());
        ObjectConverter<T> objectConverter = this.componentFactory.createConverter(className);

        return new PersistentStorage<>(objectConverter, databaseAdapter, executor);
    }
}
