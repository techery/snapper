package io.techery.snapper.storage;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.ItemRef;

public class PersistentStorage<T> implements Storage<T> {

    private final ObjectConverter<T> objectConverter;
    private final DatabaseAdapter db;
    private final Set<ItemRef<T>> itemsCache = new HashSet<>();
    private final ExecutorService executor;
    private final Object lock = new Object();

    public PersistentStorage(DatabaseAdapter db, ObjectConverter<T> objectConverter, ExecutorService executor) {
        this.db = db;
        this.objectConverter = objectConverter;
        this.executor = executor;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void put(final ItemRef<T> itemRef, final UpdateCallback<T> updateCallback) {
        putAll(Collections.singletonList(itemRef), updateCallback);
    }

    @Override
    public void putAll(final List<ItemRef<T>> items, final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                List<ItemRef<T>> added = new ArrayList<>();
                List<ItemRef<T>> updated = new ArrayList<>();
                for (ItemRef<T> itemRef : items) {
                    db.put(itemRef.getKey(), objectConverter.toBytes(itemRef.getValue()));
                    synchronized (lock) {
                        if (itemsCache.add(itemRef)) {
                            added.add(itemRef);
                        } else {
                            updated.add(itemRef);
                        }
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
        throwIfClosed();
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                for (ItemRef<T> item : items) {
                    db.delete(item.getKey());
                    synchronized (lock) {
                        itemsCache.remove(item);
                    }
                }
                updateCallback.onStorageUpdate(StorageChange.buildWithRemoved(items));
            }
        });
    }

    @Override
    public Set<ItemRef<T>> items() {
        HashSet<ItemRef<T>> itemsCopy;
        synchronized (lock) {
            itemsCopy = new HashSet<>(itemsCache);
        }
        return itemsCopy;
    }

    public void load(final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        executor.execute(new Runnable() {
            @Override public void run() {
                PersistentStorage.this.db.enumerate(new DatabaseAdapter.EnumerationCallback<ItemRef<T>>() {
                    @Override
                    public ItemRef<T> onRecord(byte[] key, byte[] value) {
                        ItemRef<T> itemRef = new ItemRef<>(key, objectConverter.fromBytes(value));
                        return itemRef;
                    }

                    @Override public void onComplete(List<ItemRef<T>> result) {
                        synchronized (lock) {
                            itemsCache.addAll(result);
                        }
                        updateCallback.onStorageUpdate(StorageChange.buildWithAdded(result));
                    }
                }, true);
            }
        });
    }

    @Override
    public void clear(final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                db.enumerate(new DatabaseAdapter.EnumerationCallback() {
                    @Override
                    public Void onRecord(byte[] key, byte[] value) {
                        db.delete(key);
                        return null;
                    }

                    @Override public void onComplete(List result) {
                        StorageChange<T> storageChange = StorageChange.buildWithRemoved(itemsCache);
                        synchronized (lock) {
                            itemsCache.clear();
                        }
                        updateCallback.onStorageUpdate(storageChange);
                    }
                }, false);

            }
        });
    }

    @Override
    public void close() {
        executor.execute(new Runnable() {
            @Override public void run() {
                executor.shutdownNow();
                itemsCache.clear();
            }
        });
    }

    private void throwIfClosed() {
        if (executor.isShutdown()) {
            throw new IllegalStateException("Storage is closed, no further operations permitted");
        }
    }
}
