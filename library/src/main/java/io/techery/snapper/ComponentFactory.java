package io.techery.snapper;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.techery.snapper.converter.KryoConverter;
import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;

public abstract class ComponentFactory {

    public abstract DatabaseAdapter createDatabase(String simpleName) throws IOException;

    public <T extends Indexable> ObjectConverter<T> createConverter(Class<T> className) {
        return new KryoConverter<>(className);
    }

    public ExecutorService createExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
