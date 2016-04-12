package io.techery.snapper.storage;

import java.io.IOException;

public interface StoragePersisterFactory {
    StoragePersister createPersister(String name) throws IOException;
}
