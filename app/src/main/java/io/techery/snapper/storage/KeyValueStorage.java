package io.techery.snapper.storage;

import java.nio.ByteBuffer;
import java.util.Set;

import io.techery.snapper.model.ItemRef;

public interface KeyValueStorage<T> {

    public interface Listener {
        public void onStorageLoaded();
    }

    public void setListener(Listener listener);

    void put(ItemRef<T> itemRef);

    void remove(ItemRef<T> itemRef);

    boolean exists(ItemRef<T> itemRef);

    boolean exists(ByteBuffer key);

    Set<ItemRef<T>> items();

    public void run(Runnable runnable);

    void clear();
}
