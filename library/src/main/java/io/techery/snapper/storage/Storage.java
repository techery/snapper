package io.techery.snapper.storage;

import java.util.List;
import java.util.Set;

import io.techery.snapper.model.ItemRef;

public interface Storage<T> {

    interface UpdateCallback<T> {
        void onStorageUpdate(StorageChange<T> storageChange);
    }

    void put(ItemRef<T> itemRef, UpdateCallback<T> updateCallback);
    void putAll(List<ItemRef<T>> items, UpdateCallback<T> updateCallback);

    void remove(ItemRef<T> itemRef, UpdateCallback<T> updateCallback);
    void removeAll(List<ItemRef<T>> items, UpdateCallback<T> updateCallback);

    void load(UpdateCallback<T> updateCallback);

    Set<ItemRef<T>> items();

    void clear(UpdateCallback<T> updateCallback);
    
    void close();
}
