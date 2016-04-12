package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.converter.ObjectConverterFactory;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.CachingStorage;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;
import io.techery.snapper.storage.StoragePersister;
import io.techery.snapper.storage.StoragePersisterFactory;
import io.techery.snapper.executor.ExecutorFactory;

public class SnappyStorageFactory implements StorageFactory {

    private final StoragePersisterFactory persisterFactory;
    private final ObjectConverterFactory converterFactory;
    private final ExecutorFactory executorFactory;

    public SnappyStorageFactory(StoragePersisterFactory persisterFactory, ObjectConverterFactory converterFactory, ExecutorFactory executorFactory) {
        this.persisterFactory = persisterFactory;
        this.converterFactory = converterFactory;
        this.executorFactory = executorFactory;
    }

    @Override
    public <T extends Indexable> Storage<T> createStorage(Class<T> clazz, String name) throws IOException {
        StoragePersister storagePersister = persisterFactory.createPersister(name);
        ObjectConverter<T> objectConverter = converterFactory.createConverter(clazz);
        ExecutorService executor = executorFactory.createExecutor();

        return new CachingStorage<T>(storagePersister, objectConverter, executor);
    }

}
