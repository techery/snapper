package io.techery.snapper.datacollection.naming;

import io.techery.snapper.model.Indexable;

public interface DataCollectionNamingFactory {
    <T extends Indexable> String createName(Class<T> clazz, String label);
}
