package io.techery.snapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.KeyValueStorageFactory;

public abstract class Snapper {

    private final KeyValueStorageFactory keyValueStorageFactory;
    private final Map<Class, DataCollection> dataCollectionCache = new HashMap<>();

    public Snapper(KeyValueStorageFactory keyValueStorageFactory) {
        this.keyValueStorageFactory = keyValueStorageFactory;
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> className) throws IOException {

        DataCollection<T> dataCollection = dataCollectionCache.get(className);

        if (dataCollection == null) {
            dataCollection = new DataCollection<>(keyValueStorageFactory.createStorage(className));
            dataCollectionCache.put(className, dataCollection);
        }

        return dataCollection;
    }
}
