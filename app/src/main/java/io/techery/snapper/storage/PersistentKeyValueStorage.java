package io.techery.snapper.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
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

    public PersistentKeyValueStorage(ObjectConverter<T> objectConverter, DatabaseFactory databaseFactory, Executor executor) {
        this.objectConverter = objectConverter;
        this.executor = executor;

        try {
            this.db = databaseFactory.createDatabase(getClass().getName());
            this.executor.execute(new Runnable() {
                @Override
                public void run() {
                    loadFromDisk();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void put(final ItemRef<T> itemRef) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        final Output output = new Output(stream);
        try {
            this.kryo.writeObject(output, itemRef.getValue());
            output.close();

            this.executor.execute(new Runnable() {
                @Override
                public void run() {
                    db.put(itemRef.getKey().array(), objectConverter.toBytes(itemRef.getValue()));
                }
            });

            this.itemsCache.add(itemRef);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public Set<ItemRef<T>> items() {
        return this.itemsCache;
    }

    public void loadFromDisk() {
        this.db.enumerate(new DatabaseAdapter.EnumerationCallback() {
            @Override
            public void onRecord(byte[] key, byte[] value) {
                final Input input = new Input(value);
                T parsedValue = null;
                try {
                    parsedValue = kryo.readObject(input, className);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (parsedValue != null) {
                    itemsCache.add(new ItemRef<>(ByteBuffer.wrap(key), objectConverter.fr));
                }

            }
        });
    }

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
                this.db = null;
            }
        }
    }
}
