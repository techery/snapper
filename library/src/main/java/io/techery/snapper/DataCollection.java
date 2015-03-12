package io.techery.snapper;


import com.innahema.collections.query.functions.Function1;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.ListUtils;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

public class DataCollection<T extends Indexable> extends DataSet<T> implements Storage.UpdateCallback<T> {

    private final Storage<T> storage;
    private final Executor executor;
    private static final ExecutorService parentExecutor;

    static {
        parentExecutor = Executors.newFixedThreadPool(4);
    }

    public DataCollection(Storage<T> storage) {
        this.storage = storage;
        this.executor = new CollectionExecutor(parentExecutor);
        this.storage.load(this);
    }

    public IDataView.Builder<T> view() {
        return new DataViewBuilder<>(this);
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

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return storage.items().iterator();
    }

    @Override
    public void perform(Runnable runnable) {
        executor.execute(runnable);
    }

    @Override
    public void onStorageUpdate(final StorageChange<T> storageChange) {
        executor.execute(new Runnable() {
            @Override public void run() {
                DataCollection.this.didUpdateDataSet(storageChange);
            }
        });
    }

    private static class CollectionExecutor implements Executor {

        ExecutorService executor;
        LinkedBlockingQueue<Runnable> queue;
        volatile boolean canConsume = true;
        ReentrantLock lock;

        private CollectionExecutor(ExecutorService executor) {
            this.executor = executor;
            this.queue = new LinkedBlockingQueue();
            lock = new ReentrantLock();
        }

        @Override public void execute(Runnable command) {
            try {
                queue.put(command);
                lock.lock();
                if (!canConsume) {
                    lock.unlock();
                    return;
                }
                canConsume = false;
                lock.unlock();
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
