package io.techery.snapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageFactory;

public class Snapper {

    private final StorageFactory storageFactory;
    private final ComponentFactory componentFactory;
    private final Map<String, DataCollection> dataCollectionCache;
    private final Map<DataCollection, Storage> collectionStorageCache;

    public Snapper(StorageFactory storageFactory, ComponentFactory componentFactory) {
        this.storageFactory = storageFactory;
        this.componentFactory = componentFactory;
        this.dataCollectionCache = new HashMap<>();
        this.collectionStorageCache = new HashMap<>();
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> className) {
        return collection(className, null);
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> className, String prefix) {
        String storageName = storageFactory.buildStorageName(className, prefix);
        //
        DataCollection<T> dataCollection = dataCollectionCache.get(storageName);
        if (dataCollection == null || dataCollection.isClosed()) {
            // Double check not to create duplicates from sep. threads
            synchronized (Snapper.class) {
                dataCollection = dataCollectionCache.get(storageName);
                if (dataCollection == null || dataCollection.isClosed()) {
                    try {
                        if (dataCollection != null) {
                            collectionStorageCache.remove(dataCollection);
                        }
                        Storage<T> storage = storageFactory.createStorage(className, prefix);
                        Executor executor = componentFactory.createCollectionExecutor();
                        dataCollection = new DataCollection<>(storage, executor);
                        //
                        dataCollectionCache.put(storageName, dataCollection);
                        collectionStorageCache.put(dataCollection, storage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return dataCollection;
    }

    public void clear() {
        synchronized (Snapper.class) {
            for (DataCollection dataCollection : collectionStorageCache.keySet()) {
                dataCollection.clear();
            }
        }
    }

    public void close() {
        synchronized (Snapper.class) {
            for (DataCollection collection : collectionStorageCache.keySet()) {
                Storage storage = collectionStorageCache.get(collection);
                storage.close();
                collection.close();
            }
            collectionStorageCache.clear();
        }
    }
}
