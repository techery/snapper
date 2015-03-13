package io.techery.snapper;

import java.io.IOException;
import java.util.concurrent.Executor;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.storage.DatabaseAdapter;

public interface ComponentFactory {

    DatabaseAdapter createDatabase(String simpleName) throws IOException;
    <T extends Indexable> ObjectConverter<T> createConverter(Class<T> className);
    Executor createStorageExecutor();
    Executor createCollectionExecutor();
}
