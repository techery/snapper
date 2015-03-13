package io.techery.snapper.storage;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
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
    private final DatabaseAdapter db;
    private final Set<ItemRef<T>> itemsCache = new HashSet<>();
    private final Executor executor;

    public PersistentStorage(DatabaseAdapter db, ObjectConverter<T> objectConverter, Executor executor) {
        this.db = db;
        this.objectConverter = objectConverter;
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
                    db.put(itemRef.getKey(), objectConverter.toBytes(itemRef.getValue()));
                    boolean isUpdate = itemsCache.contains(itemRef);
                    itemsCache.add(itemRef);
                    if (isUpdate) {
                        updated.add(itemRef);
                    } else {
                        added.add(itemRef);
                    }
                }
                Log.i("Storage", "Insert:" + added.size());
                Log.i("Storage", "Update:" + updated.size());
                StorageChange<T> storageChange = new StorageChange<>(added, updated, Collections.<ItemRef<T>>emptyList());
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
                    db.delete(itemRef.getKey());
                }
                itemsCache.removeAll(items);
                updateCallback.onStorageUpdate(StorageChange.buildWithRemoved(items));
            }
        });
    }

    @Override
    public Set<ItemRef<T>> items() {
        return this.itemsCache;
    }

    public void load(final UpdateCallback<T> updateCallback) {
        executor.execute(new Runnable() {
            @Override public void run() {
                PersistentStorage.this.db.enumerate(new DatabaseAdapter.EnumerationCallback() {
                    @Override
                    public ItemRef<T> onRecord(byte[] key, byte[] value) {
                        ItemRef<T> itemRef = new ItemRef<>(key, objectConverter.fromBytes(value));
                        return itemRef;
                    }

                    @Override public void onBatchComplete(List result) {
                        itemsCache.addAll(result);
                        updateCallback.onStorageUpdate(StorageChange.buildWithAdded(result));
                    }

                    @Override public void onComplete(List result) {
                    }
                }, true);
            }
        });
    }

    @Override
    public void clear(final UpdateCallback<T> updateCallback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                db.enumerate(new DatabaseAdapter.EnumerationCallback() {
                    @Override
                    public Void onRecord(byte[] key, byte[] value) {
                        db.delete(key);
                        return null;
                    }

                    @Override public void onBatchComplete(List result) {
                    }

                    @Override public void onComplete(List result) {
                        StorageChange<T> storageChange = StorageChange.buildWithRemoved(itemsCache);
                        itemsCache.clear();
                        updateCallback.onStorageUpdate(storageChange);
                    }
                }, false);

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
