package io.techery.snapper.storage;

import java.io.IOException;
import java.util.List;

public interface DatabaseAdapter {

    void close() throws IOException;

    public interface EnumerationCallback<T> {
        public T onRecord(byte[] key, byte[] value);
        public void onBatchComplete(List<T> result);
        public void onComplete(List<T> result);
    }

    void put(byte[] array, byte[] bytes);

    void delete(byte[] array);

    void enumerate(EnumerationCallback enumerationCallback, boolean withValue);
}
