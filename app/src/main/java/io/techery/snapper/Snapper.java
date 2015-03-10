package io.techery.snapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.StorageFactory;

public abstract class Snapper {

    private final StorageFactory storageFactory;
    private final Map<Class<? extends Indexable>, DataCollection> dataCollectionCache;

    public Snapper(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
        dataCollectionCache = new HashMap<>();
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> className) {
        DataCollection<T> dataCollection = dataCollectionCache.get(className);
        if (dataCollection == null) {
            // Double check not to create duplicates from sep. threads
            synchronized (Snapper.class) {
                dataCollection = dataCollectionCache.get(className);
                if (dataCollection == null) {
                    try {
                        dataCollection = new DataCollection<>(storageFactory.createStorage(className));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                    dataCollectionCache.put(className, dataCollection);
                }
            }
        }
        return dataCollection;
    }
}
