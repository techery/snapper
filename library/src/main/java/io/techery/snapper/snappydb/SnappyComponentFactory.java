package io.techery.snapper.snappydb;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.converter.KryoConverter;
import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.DatabaseFactory;

public abstract class SnappyComponentFactory implements ComponentFactory {

    private final DatabaseFactory databaseFactory;

    public SnappyComponentFactory(DatabaseFactory databaseFactory) {
        this.databaseFactory = databaseFactory;
    }

    @Override
    public DatabaseAdapter createDatabase(String simpleName) throws IOException {
        return databaseFactory.createDatabase(simpleName);
    }

    @Override
    public <T extends Indexable> ObjectConverter<T> createConverter(Class<T> className) {
        return new KryoConverter<>(className);
    }

    public static class DefaultSnappyComponentFactory extends SnappyComponentFactory {

        private final ExecutorService storageExecutor = Executors.newSingleThreadExecutor();
        private final ExecutorService collectionExecutor = Executors.newFixedThreadPool(4);

        public DefaultSnappyComponentFactory(DatabaseFactory snappyDBFactory) {
            super(snappyDBFactory);
        }

        @Override
        public ExecutorService createStorageExecutor() {
            return storageExecutor;
        }

        @Override
        public ExecutorService createCollectionExecutor() {
            return collectionExecutor;
        }
    }

}
