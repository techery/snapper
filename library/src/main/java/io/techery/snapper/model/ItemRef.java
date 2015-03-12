package io.techery.snapper.model;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

public class ItemRef<T> {
    private final ByteBuffer key;
    private final T value;
    private final int keyHash;

    public static <T extends Indexable> ItemRef<T> make(T item) {
        return new ItemRef<>(item.index(), item);
    }

    public ItemRef(ByteBuffer key, T value) {

        if (key.capacity() != 4) {
            throw new InvalidParameterException("Invalid key:" + key);
        }

        this.key = key;
        this.value = value;
        key.rewind();
        keyHash = key.getInt();
        key.rewind();
    }

    public T getValue() {
        return value;
    }

    public ByteBuffer getKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ItemRef)) {
            return false;
        }

        return getKey().compareTo(((ItemRef<T>) obj).getKey()) == 0;
    }

    @Override
    public int hashCode() {
        return keyHash;
    }
}
