package io.techery.snapper.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.ItemRef;

public class CachingStorage<T> implements Storage<T> {

    private final StoragePersister persister;
    private final ObjectConverter<T> objectConverter;
    private final Set<ItemRef<T>> itemsCache;
    //
    private final ExecutorService executor;
    private final Set<Future> tasks;
    private final Object lock = new Object();
    //
    private volatile boolean isLoaded;
    private volatile boolean isClosed;

    public CachingStorage(StoragePersister<T> persister, ObjectConverter<T> objectConverter, ExecutorService executor) {
        this.persister = persister;
        this.objectConverter = objectConverter;
        this.executor = executor;
        itemsCache = new HashSet<ItemRef<T>>();
        tasks = Collections.newSetFromMap(new WeakHashMap<Future, Boolean>());
    }

    @Override
    protected void finalize() throws Throwable {
        if (!isClosed) close();
        super.finalize();
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void put(final ItemRef<T> itemRef, final UpdateCallback<T> updateCallback) {
        putAll(Collections.singletonList(itemRef), updateCallback);
    }

    @Override
    public void putAll(final List<ItemRef<T>> items, final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        submit(new Runnable() {
            @Override
            public void run() {
                List<ItemRef<T>> added = new ArrayList<ItemRef<T>>();
                List<ItemRef<T>> updated = new ArrayList<ItemRef<T>>();
                for (ItemRef<T> itemRef : items) {
                    byte[] bytes;
                    try {
                        bytes = objectConverter.toBytes(itemRef.getValue());
                    } catch (Exception e) {
                        // Log.w(TAG, "Serialization failed on put", e);
                        continue;
                    }
                    persister.put(itemRef.getKey(), bytes);
                    synchronized (lock) {
                        if (itemsCache.add(itemRef)) {
                            added.add(itemRef);
                        } else {
                            itemsCache.remove(itemRef);
                            itemsCache.add(itemRef);
                            updated.add(itemRef);
                        }
                    }
                }
                // Log.i("Storage", "Insert:" + added.size());
                // Log.i("Storage", "Update:" + updated.size());
                StorageChange<T> storageChange = new StorageChange<T>(added, updated, Collections.<ItemRef<T>>emptyList());
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
        submit(new Runnable() {
            @Override
            public void run() {
                for (ItemRef<T> item : items) {
                    persister.delete(item.getKey());
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
            itemsCopy = new HashSet<ItemRef<T>>(itemsCache);
        }
        return itemsCopy;
    }

    public void load(final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        submit(new Runnable() {
            @Override
            public void run() {
                CachingStorage.this.persister.enumerate(new StoragePersister.EnumerationCallback<ItemRef<T>>() {
                    @Override
                    public ItemRef<T> onRecord(byte[] key, byte[] value) {
                        T item;
                        try {
                            item = objectConverter.fromBytes(value);
                        } catch (Exception e) {
                            // Log.w(TAG, "Deserialization failed on load", e);
                            return null;
                        }
                        ItemRef<T> itemRef = new ItemRef<T>(key, item);
                        return itemRef;
                    }

                    @Override
                    public void onComplete(List<ItemRef<T>> result) {
                        synchronized (lock) {
                            itemsCache.addAll(result);
                        }
                        isLoaded = true;
                        updateCallback.onStorageUpdate(StorageChange.buildWithAdded(result));
                    }
                }, true);
            }
        });
    }

    @Override
    public void clear(final UpdateCallback<T> updateCallback) {
        throwIfClosed();
        submit(new Runnable() {
            @Override
            public void run() {
                persister.enumerate(new StoragePersister.EnumerationCallback() {
                    @Override
                    public Void onRecord(byte[] key, byte[] value) {
                        persister.delete(key);
                        return null;
                    }

                    @Override
                    public void onComplete(List result) {
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
        synchronized (lock) {
            isLoaded = false;
            isClosed = true;
            itemsCache.clear();
            for (Future task : tasks) {
                task.cancel(true);
            }
        }
    }

    private void submit(Runnable runnable) {
        Future<?> future = executor.submit(runnable);
        tasks.add(future);
    }

    private void throwIfClosed() {
        synchronized (lock) {
            if (isClosed) {
                throw new IllegalStateException("Storage is closed, no further operations permitted");
            }
        }
    }
}
