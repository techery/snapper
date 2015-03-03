package io.techery.snapper.storage;

import java.io.IOException;

public interface DatabaseAdapter {

    void close() throws IOException;

    public interface EnumerationCallback {
        public void onRecord(byte[] key, byte[] value);
    }

    void put(byte[] array, byte[] bytes);

    void delete(byte[] array);

    void enumerate(EnumerationCallback enumerationCallback);
}
