package io.techery.snapper.datacollection;

import io.techery.snapper.model.Indexable;

public interface DataCollectionFactory {
    <T extends Indexable> DataCollection<T> createCollection(Class<T> clazz, String name) throws Throwable;
}
