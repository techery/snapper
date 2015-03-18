package io.techery.snapper.storage;

import java.io.IOException;
import java.util.List;

public interface DatabaseAdapter {

    void close() throws IOException;

    void put(byte[] array, byte[] bytes);

    void delete(byte[] array);

    <T> void enumerate(EnumerationCallback<T> enumerationCallback, boolean withValue);

    interface EnumerationCallback<T> {
        T onRecord(byte[] key, byte[] value);
        void onComplete(List<T> result);
    }
}
