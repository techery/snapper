package io.techery.snapper.model;

import java.util.Arrays;

public class ItemRef<T> {
    private final byte[] key;
    private final T value;
    private final int keyHash;

    public static <T extends Indexable> ItemRef<T> make(T item) {
        return new ItemRef<>(item.index(), item);
    }

    public <U> ItemRef<U> withValue(U item) {
        return new ItemRef<>(key, item, keyHash);
    }

    public ItemRef(byte[] key, T value) {
        this(key, value, Arrays.hashCode(key));
    }

    public ItemRef(byte[] key, T value, int keyHash) {
        this.value = value;
        this.key = key;
        this.keyHash = keyHash;
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
