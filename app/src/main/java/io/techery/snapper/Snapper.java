package io.techery.snapper;

import java.io.IOException;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.KeyValueStorageFactory;

public class Snapper {

    private final KeyValueStorageFactory keyValueStorageFactory;

    public Snapper(KeyValueStorageFactory keyValueStorageFactory) {
        this.keyValueStorageFactory = keyValueStorageFactory;
    }

    public <T extends Indexable> DataCollection<T> createCollection(Class<T> className) throws IOException {
        return new DataCollection<>(keyValueStorageFactory.createStorage(className));
    }
}
