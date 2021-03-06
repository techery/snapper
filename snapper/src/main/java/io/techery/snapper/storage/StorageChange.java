package io.techery.snapper.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.techery.snapper.model.ItemRef;

public class StorageChange<T> {
    private final List<ItemRef<T>> added;
    private final List<ItemRef<T>> updated;
    private final List<ItemRef<T>> removed;

    public StorageChange(List<ItemRef<T>> added, List<ItemRef<T>> updated, List<ItemRef<T>> removed) {
        this.added = added;
        this.updated = updated;
        this.removed = removed;
    }

    public static <T> StorageChange<T> buildWithRemoved(Collection<ItemRef<T>> removed) {
        return new StorageChange<T>(Collections.<ItemRef<T>>emptyList(), Collections.<ItemRef<T>>emptyList(), new ArrayList<ItemRef<T>>(removed));
    }

    public static <T> StorageChange<T> buildWithAdded(Collection<ItemRef<T>> added) {
        return new StorageChange<T>(new ArrayList<ItemRef<T>>(added), Collections.<ItemRef<T>>emptyList(), Collections.<ItemRef<T>>emptyList());
    }

    public static <T> StorageChange<T> buildWithUpdated(Collection<ItemRef<T>> updated) {
        return new StorageChange<T>(Collections.<ItemRef<T>>emptyList(), new ArrayList<ItemRef<T>>(updated), Collections.<ItemRef<T>>emptyList());
    }

    public static <T> StorageChange<T> empty() {
        return new StorageChange<T>(
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

    public boolean isEmpty() {
        return added.isEmpty() && updated.isEmpty() && removed.isEmpty();
    }

    @Override public String toString() {
        return "StorageChange{" +
                "added=" + added.size() +
                ", updated=" + updated.size() +
                ", removed=" + removed.size() +
                '}';
    }
}
