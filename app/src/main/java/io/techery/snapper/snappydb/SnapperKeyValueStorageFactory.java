package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.KeyValueStorage;
import io.techery.snapper.storage.KeyValueStorageFactory;
import io.techery.snapper.storage.PersistentKeyValueStorage;

public class SnapperKeyValueStorageFactory implements KeyValueStorageFactory {
    private final ComponentFactory componentFactory;

    public SnapperKeyValueStorageFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    public <T extends Indexable> KeyValueStorage<T> createStorage(Class<T> className) throws IOException {

        ExecutorService executor = this.componentFactory.createExecutor();
        DatabaseAdapter databaseAdapter = this.componentFactory.createDatabase(className.getSimpleName());
        ObjectConverter<T> objectConverter = this.componentFactory.createConverter(className);

        return new PersistentKeyValueStorage<>(objectConverter, databaseAdapter, executor);
    }
}
