package io.techery.snapper.storage;

import java.util.Collections;
import java.util.List;

import io.techery.snapper.model.ItemRef;

public class StorageChange<T> {
    private final List<ItemRef<T>> added;
    private final List<ItemRef<T>> updated;
    private final List<ItemRef<T>> removed;

    public StorageChange(final List<ItemRef<T>> added, final List<ItemRef<T>> updated, final List<ItemRef<T>> removed) {
        this.added = added;
        this.updated = updated;
        this.removed = removed;
    }

    public static <T> StorageChange<T> buildWithRemoved(final List<ItemRef<T>> removed) {
        return new StorageChange<>(Collections.<ItemRef<T>>emptyList(), Collections.<ItemRef<T>>emptyList(), removed);
    }

    public static <T> StorageChange<T> buildWithAdded(final List<ItemRef<T>> added) {
        return new StorageChange<>(added, Collections.<ItemRef<T>>emptyList(), Collections.<ItemRef<T>>emptyList());
    }

    public static <T> StorageChange<T> buildWithUpdated(final List<ItemRef<T>> updated) {
        return new StorageChange<>(Collections.<ItemRef<T>>emptyList(), updated, Collections.<ItemRef<T>>emptyList());
    }

    public static <T> StorageChange<T> empty() {
        return new StorageChange<>(
                Collections.<ItemRef<T>>emptyList(),
                Collections.<ItemRef<T>>emptyList(),
                Collections.<ItemRef<T>>emptyList());
    }

    public List<ItemRef<T>> getAdded() {
        return this.added;
    }

    public List<ItemRef<T>> getUpdated() {
        return this.updated;
    }

    public List<ItemRef<T>> getRemoved() {
        return this.removed;
    }
}
