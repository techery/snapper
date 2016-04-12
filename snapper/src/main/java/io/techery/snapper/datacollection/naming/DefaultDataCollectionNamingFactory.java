package io.techery.snapper.datacollection.naming;

import io.techery.snapper.model.Indexable;

public class DefaultDataCollectionNamingFactory implements DataCollectionNamingFactory {

    @Override
    public <T extends Indexable> String createName(Class<T> clazz, String label) {
        StringBuilder databaseName = new StringBuilder(clazz.getSimpleName());
        if (label != null) {
            databaseName.append('_').append(label);
        }
        return databaseName.toString();
    }
}
