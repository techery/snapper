package io.techery.snapper.storage;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.ItemRef;

public class PersistentStorage<T> implements Storage<T>, Closeable {

    private final ObjectConverter<T> objectConverter;
    private DatabaseAdapter db;
    private final Set<ItemRef<T>> itemsCache = new HashSet<>();
    private final Executor executor;

    public PersistentStorage(ObjectConverter<T> objectConverter, DatabaseAdapter db, Executor executor) {
        this.objectConverter = objectConverter;
        this.db = db;
        this.executor = executor;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void put(final ItemRef<T> itemRef, final UpdateCallback<T> updateCallback) {
        putAll(Collections.singletonList(itemRef), updateCallback);
    }

    @Override
    public void putAll(final List<ItemRef<T>> items, final UpdateCallback<T> updateCallback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {

                List<ItemRef<T>> added = new ArrayList<>();
                List<ItemRef<T>> updated = new ArrayList<>();

                for (ItemRef<T> itemRef : items) {
                    db.put(itemRef.getKey().array(), objectConverter.toBytes(itemRef.getValue()));

                    boolean isUpdate = itemsCache.contains(itemRef);

                    itemsCache.add(itemRef);

                    if (isUpdate) {
                        updated.add(itemRef);
                    } else {
                        added.add(itemRef);
                    }
                }

                StorageChange<T> storageChange = new StorageChange<>(added, updated, new ArrayList<ItemRef<T>>());

                Log.d("Storage", "Insert:" + added.size());

                updateCallback.onStorageUpdate(storageChange);
            }
        });
    }

    @Override
    public void remove(final ItemRef<T> itemRef, final UpdateCallback<T> updateCallback) {
        removeAll(Collections.singletonList(itemRef), updateCallback);
    }

    @Override
    public void removeAll(final List<ItemRef<T>> items, final UpdateCallback<T> updateCallback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {

                for (ItemRef<T> itemRef : items) {
                    db.delete(itemRef.getKey().array());
                    itemsCache.remove(itemRef);
                }

                updateCallback.onStorageUpdate(StorageChange.buildWithRemoved(items));
            }
        });
    }

    @Override
    public Set<ItemRef<T>> items() {
        return this.itemsCache;
    }

    public void load(final UpdateCallback<T> updateCallback) {
        this.db.enumerate(new DatabaseAdapter.EnumerationCallback() {
            @Override
            public void onRecord(byte[] key, byte[] value) {
                itemsCache.add(new ItemRef<>(ByteBuffer.wrap(key), objectConverter.fromBytes(value)));

                final ArrayList<ItemRef<T>> added = new ArrayList<>();
                added.addAll(itemsCache);

                updateCallback.onStorageUpdate(StorageChange.buildWithAdded(added));
            }
        });
    }

    @Override
    public void clear(final UpdateCallback<T> updateCallback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {

                final ArrayList<ItemRef<T>> removed = new ArrayList<>();
                removed.addAll(itemsCache);

                db.enumerate(new DatabaseAdapter.EnumerationCallback() {
                    @Override
                    public void onRecord(byte[] key, byte[] value) {
                        db.delete(key);
                    }
                });

                itemsCache.clear();

                updateCallback.onStorageUpdate(StorageChange.buildWithRemoved(removed));
            }
        });
    }

    @Override
    public void close() throws IOException {
        if (this.db != null) {
            try {
                this.db.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
    }
}
