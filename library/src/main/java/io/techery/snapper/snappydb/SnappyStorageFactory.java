package io.techery.snapper.snappydb;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

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
    public <T extends Indexable> Storage<T> createStorage(Class<T> className, String prefix) throws IOException {
        String storageName = buildStorageName(className, prefix);
        DatabaseAdapter databaseAdapter = this.componentFactory.createDatabase(storageName);
        ObjectConverter<T> objectConverter = this.componentFactory.createConverter(className);
        ExecutorService executor = this.componentFactory.createStorageExecutor();

        return new PersistentStorage<>(databaseAdapter, objectConverter, executor);
    }

    @Override public <T extends Indexable> String buildStorageName(Class<T> className, String prefix) {
        StringBuilder databaseName = new StringBuilder(className.getSimpleName());
        if (!TextUtils.isEmpty(prefix)) {
            databaseName.append('_').append(prefix);
        }
        return databaseName.toString();
    }
}
