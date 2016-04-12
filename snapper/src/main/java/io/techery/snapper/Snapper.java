package io.techery.snapper;

import java.util.HashMap;
import java.util.Map;

import io.techery.snapper.datacollection.DataCollection;
import io.techery.snapper.datacollection.DataCollectionFactory;
import io.techery.snapper.datacollection.naming.DataCollectionNamingFactory;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.sweeper.Sweeper;

public class Snapper {

    private final DataCollectionNamingFactory collectionNamingFactory;
    private final DataCollectionFactory collectionFactory;
    private final Sweeper sweeper;
    private final Map<String, DataCollection> dataCollectionCache;

    public Snapper(DataCollectionNamingFactory collectionNamingFactory, DataCollectionFactory collectionFactory) {
        this(collectionNamingFactory, collectionFactory, null);
    }

    public Snapper(DataCollectionNamingFactory collectionNamingFactory, DataCollectionFactory collectionFactory, Sweeper sweeper) {
        this.collectionNamingFactory = collectionNamingFactory;
        this.collectionFactory = collectionFactory;
        this.sweeper = sweeper;
        this.dataCollectionCache = new HashMap<String, DataCollection>();
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> clazz) {
        return collection(clazz, null);
    }

    public <T extends Indexable> DataCollection<T> collection(Class<T> clazz, String prefix) {
        String collectionName = collectionNamingFactory.createName(clazz, prefix);
        //
        DataCollection<T> dataCollection = dataCollectionCache.get(collectionName);
        if (dataCollection == null || dataCollection.isClosed()) {
            // Double check not to create duplicates from sep. threads
            synchronized (Snapper.class) {
                dataCollection = dataCollectionCache.get(collectionName);
                if (dataCollection == null || dataCollection.isClosed()) {
                    try {
                        dataCollection = collectionFactory.createCollection(clazz, collectionName);
                    } catch (Throwable throwable) {
                        throw new RuntimeException("Can't create collection", throwable);
                    }
                    dataCollectionCache.put(collectionName, dataCollection);
                }
            }
        }
        return dataCollection;
    }

    public void clear() {
        synchronized (Snapper.class) {
            for (DataCollection dataCollection : dataCollectionCache.values()) {
                if (!dataCollection.isClosed()) dataCollection.clear();
            }
            if (sweeper != null) sweeper.clear();
        }
    }

    public void close() {
        synchronized (Snapper.class) {
            for (DataCollection collection : dataCollectionCache.values()) {
                if (!collection.isClosed()) collection.close();
            }
            dataCollectionCache.clear();
        }
    }
}
