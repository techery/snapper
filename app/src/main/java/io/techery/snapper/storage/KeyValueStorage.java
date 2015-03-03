package io.techery.snapper.storage;

import java.util.Set;

import io.techery.snapper.model.ItemRef;

public interface KeyValueStorage<T> {

    public interface Listener {
        public void onStorageLoaded();
    }

    public void setListener(Listener listener);

    void put(ItemRef<T> itemRef);

    void remove(ItemRef<T> itemRef);

    Set<ItemRef<T>> items();
}
