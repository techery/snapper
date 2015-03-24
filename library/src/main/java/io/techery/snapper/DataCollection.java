package io.techery.snapper;


import com.innahema.collections.query.functions.Function1;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.ListUtils;
import io.techery.snapper.util.SimpleExecutorService;
import io.techery.snapper.projection.ProjectionBuilder;
import io.techery.snapper.projection.IProjection;

public class DataCollection<T extends Indexable> extends DataSet<T> implements Storage.UpdateCallback<T> {

    private final Storage<T> storage;
    private final ExecutorService executor;

    public DataCollection(Storage<T> storage, Executor executor) {
        this.storage = storage;
        this.executor = new CollectionExecutor(executor);
        this.storage.load(this);
    }

    @Override
    public void close() {
        executor.shutdown();
        clearDataListeners();
        didClose();
        clearStatusListeners();
    }

    @Override
    public boolean isClosed() {
        return executor.isShutdown();
    }

    public IProjection.Builder<T> projection() {
        return new ProjectionBuilder<>(this);
    }

    public void insert(final T item) {
        storage.put(ItemRef.make(item), this);
    }

    public void insertAll(final List<T> items) {
        storage.putAll(ListUtils.map(items, new Function1<T, ItemRef<T>>() {
            @Override
            public ItemRef<T> apply(T t) {
                return ItemRef.make(t);
            }
        }), this);
    }

    public void remove(final T item) {
        storage.remove(ItemRef.make(item), this);
    }

    public void clear() {
        storage.clear(this);
    }

    @Override public List<T> toList() {
        return ListUtils.map(storage.items(), new Function1<ItemRef<T>, T>() {
            @Override public T apply(ItemRef<T> tItemRef) {
                return tItemRef.getValue();
            }
        });
    }

    @Override public int size() {
        return storage.items().size();
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return storage.items().iterator();
    }

    @Override
    public void perform(Runnable runnable) {
        if (executor.isShutdown()) throw new IllegalStateException("Collection closed, no further op. permitted");
        executor.execute(runnable);
    }

    @Override
    public void onStorageUpdate(final StorageChange<T> storageChange) {
        if (executor.isShutdown()) return;
        executor.execute(new Runnable() {
            @Override public void run() {
                DataCollection.this.didUpdateDataSet(storageChange);
            }
        });
    }

    private static class CollectionExecutor extends SimpleExecutorService {

        Executor executor;
        ConcurrentLinkedQueue<Runnable> queue;
        volatile boolean canConsume = true;
        ReentrantLock lock;

        private CollectionExecutor(Executor executor) {
            this.executor = executor;
            this.queue = new ConcurrentLinkedQueue();
            lock = new ReentrantLock();
        }

        @Override
        public void execute(Runnable command) {
            lock.lock();
            queue.add(command);
            if (!canConsume) {
                lock.unlock();
                return;
            }
            canConsume = false;
            executor.execute(new Runnable() {
                @Override public void run() {
                    lock.lock();
                    Runnable firstCommand = queue.poll();
                    if (firstCommand != null) {
                        lock.unlock();
                        firstCommand.run();
                        executor.execute(this);
                    } else {
                        canConsume = true;
                        lock.unlock();
                    }
                }
            });
            lock.unlock();
        }
    }
}
