package io.techery.snapper.model;

import java.util.Arrays;

public class ItemRef<T> {
    private final byte[] key;
    private final T value;
    private final int keyHash;

    public static <T extends Indexable> ItemRef<T> make(T item) {
        return new ItemRef<>(item.index(), item);
    }

    public ItemRef(byte[] key, T value) {
        this.value = value;
        this.key = key;
        this.keyHash = Arrays.hashCode(key);
    }

    public T getValue() {
        return value;
    }

    public byte[] getKey() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemRef other = (ItemRef) obj;
        if (!Arrays.equals(key, other.key))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return keyHash;
    }
}
