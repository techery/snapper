package io.techery.snapper.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import io.techery.snapper.converter.ObjectConverter;
import io.techery.snapper.model.ItemRef;

public class PersistentKeyValueStorage<T> implements KeyValueStorage<T>, Closeable {

    private final ObjectConverter<T> objectConverter;
    private DatabaseAdapter db;
    private final Set<ItemRef<T>> itemsCache = new HashSet<>();
    private final Executor executor;

    public PersistentKeyValueStorage(ObjectConverter<T> objectConverter, DatabaseAdapter db, Executor executor) {
        this.objectConverter = objectConverter;
        this.db = db;
        this.executor = executor;
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                loadFromDisk();
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void setListener(Listener listener) {

    }

    @Override
    public void put(final ItemRef<T> itemRef) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                db.put(itemRef.getKey().array(), objectConverter.toBytes(itemRef.getValue()));
                itemsCache.add(itemRef);
            }
        });
    }

    @Override
    public void remove(final ItemRef<T> itemRef) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                db.delete(itemRef.getKey().array());
            }
        });
        this.itemsCache.remove(itemRef);
    }

    @Override
    public boolean exists(ItemRef<T> itemRef) {
        return exists(itemRef.getKey());
    }

    @Override
    public boolean exists(ByteBuffer key) {
        return this.itemsCache.contains(new ItemRef<T>(key, null));
    }

    @Override
    public Set<ItemRef<T>> items() {
        return this.itemsCache;
    }

    @Override
    public void run(Runnable runnable) {
        this.executor.execute(runnable);
    }

    public void loadFromDisk() {
        this.db.enumerate(new DatabaseAdapter.EnumerationCallback() {
            @Override
            public void onRecord(byte[] key, byte[] value) {
                itemsCache.add(new ItemRef<>(ByteBuffer.wrap(key), objectConverter.fromBytes(value)));
            }
        });
    }

    @Override
    public void clear() {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {

                db.enumerate(new DatabaseAdapter.EnumerationCallback() {
                    @Override
                    public void onRecord(byte[] key, byte[] value) {
                        db.delete(key);
                    }
                });

                itemsCache.clear();
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
