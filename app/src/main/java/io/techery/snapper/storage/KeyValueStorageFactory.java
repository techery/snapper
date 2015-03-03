package io.techery.snapper.storage;

import java.io.IOException;

import io.techery.snapper.model.Indexable;

public interface KeyValueStorageFactory {
    <T extends Indexable> KeyValueStorage<T> createStorage(Class<T> className) throws IOException;
}
