package io.techery.snapper.dataset;

import java.util.ArrayList;
import java.util.List;

import io.techery.snapper.model.ItemRef;

public class DataSetChange<T> {
    private final List<ItemRef<T>> addedItems;
    private final List<ItemRef<T>> updatedItems;
    private final List<ItemRef<T>> removedItems;

    public DataSetChange(final List<ItemRef<T>> addedItems, final List<ItemRef<T>> updatedItems, final List<ItemRef<T>> removedItems) {
        this.addedItems = addedItems;
        this.updatedItems = updatedItems;
        this.removedItems = removedItems;
    }

    public static <T> DataSetChange<T> buildWithRemoved(final List<ItemRef<T>> removed) {
        return new DataSetChange<T>(new ArrayList<ItemRef<T>>(), new ArrayList<ItemRef<T>>(), removed);
    }

    public static <T> DataSetChange<T> buildWithAdded(final List<ItemRef<T>> added) {
        return new DataSetChange<T>(added, new ArrayList<ItemRef<T>>(), new ArrayList<ItemRef<T>>());
    }

    public static <T> DataSetChange<T> buildWithUpdated(final List<ItemRef<T>> updated) {
        return new DataSetChange<T>(new ArrayList<ItemRef<T>>(), updated, new ArrayList<ItemRef<T>>());
    }

    public List<ItemRef<T>> getAdded() {
        return this.addedItems;
    }

    public List<ItemRef<T>> getUpdated() {
        return this.updatedItems;
    }

    public List<ItemRef<T>> getRemoved() {
        return this.removedItems;
    }
}
