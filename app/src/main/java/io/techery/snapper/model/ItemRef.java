package io.techery.snapper.model;

import java.nio.ByteBuffer;

public class ItemRef<T> {
    private final ByteBuffer key;
    private final T value;

    public static <T extends Indexable> ItemRef<T> make(T item) {
        return new ItemRef<T>(item.index(), item);
    }

    public ItemRef(ByteBuffer key, T value) {
        this.key = key;
        this.value = value;
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
}
