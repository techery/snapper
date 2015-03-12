package io.techery.snapper.storage;

import java.io.IOException;

import io.techery.snapper.model.Indexable;

public interface StorageFactory {
    <T extends Indexable> Storage<T> createStorage(Class<T> className) throws IOException;
}
