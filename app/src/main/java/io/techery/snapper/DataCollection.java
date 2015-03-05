package io.techery.snapper;


import com.innahema.collections.query.functions.Function1;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.Storage;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.ListUtils;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

public class DataCollection<T extends Indexable> extends DataSet<T> implements Storage.UpdateCallback<T> {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Storage<T> storage;

    public DataCollection(Storage<T> storage) {
        this.storage = storage;
        this.storage.load(this);
    }

    public IDataView.Builder<T> view() {
        return new DataViewBuilder<>(this);
    }

    public void insert(final T item) {
        this.storage.put(ItemRef.make(item), this);
    }

    public void insertAll(final List<T> items) {
        this.storage.putAll(ListUtils.map(items, new Function1<T, ItemRef<T>>() {
            @Override
            public ItemRef<T> apply(T t) {
                return ItemRef.make(t);
            }
        }), this);
    }

    public void remove(final T item) {
        this.storage.remove(ItemRef.make(item), this);
    }

    public void clear() {
        this.storage.clear(this);
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return this.storage.items().iterator();
    }

    @Override
    public void perform(Runnable runnable) {
        this.executor.execute(runnable);
    }

    @Override
    public void onStorageUpdate(final StorageChange<T> storageChange) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                DataCollection.this.didUpdateDataSet(storageChange);
            }
        });
    }
}
