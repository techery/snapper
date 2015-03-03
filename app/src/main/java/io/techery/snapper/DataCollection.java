package io.techery.snapper;


import com.innahema.collections.query.queriables.Queryable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.dataset.DataSetChange;
import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.KeyValueStorage;
import io.techery.snapper.view.DataViewBuilder;
import io.techery.snapper.view.IDataView;

public class DataCollection<T extends Indexable> extends DataSet<T> implements KeyValueStorage.Listener {

    public DataCollection(KeyValueStorage<T> keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
        this.keyValueStorage.setListener(this);
    }

    public IDataView.Builder<T> view() {
        return new DataViewBuilder<>(this);
    }

    private final KeyValueStorage<T> keyValueStorage;

    public void insert(final T item) {
        final ItemRef<T> itemRef = ItemRef.make(item);
        boolean isUpdate = this.keyValueStorage.exists(itemRef);
        this.keyValueStorage.put(itemRef);

        if (isUpdate) {
            didUpdateDataSet(DataSetChange.buildWithUpdated(Arrays.asList(itemRef)));
        } else {
            didUpdateDataSet(DataSetChange.buildWithAdded(Arrays.asList(itemRef)));
        }
    }

    public void insertAll(final List<T> items) {
        List<ItemRef<T>> updated = new ArrayList<>();
        List<ItemRef<T>> inserted = new ArrayList<>();

        for (T item : items) {
            final ItemRef<T> itemRef = ItemRef.make(item);
            boolean isUpdate = this.keyValueStorage.exists(itemRef);
            this.keyValueStorage.put(itemRef);

            if (isUpdate) {
                updated.add(itemRef);
            } else {
                inserted.add(itemRef);
            }
        }

        didUpdateDataSet(new DataSetChange<>(inserted, updated, new ArrayList<ItemRef<T>>()));
    }

    public void remove(final T item) {
        final ItemRef<T> itemRef = ItemRef.make(item);
        this.keyValueStorage.remove(itemRef);
        didUpdateDataSet(DataSetChange.buildWithRemoved(Arrays.asList(itemRef)));
    }

    public void clear() {
        List<ItemRef<T>> deletedItems = Queryable.from(this).toList();

        this.keyValueStorage.clear();

        didUpdateDataSet(DataSetChange.buildWithRemoved(deletedItems));
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return this.keyValueStorage.items().iterator();
    }

    @Override
    public void onStorageLoaded() {
        didUpdateDataSet(DataSetChange.buildWithAdded(Queryable.from(this.keyValueStorage.items()).toList()));
    }

    @Override
    public void run(Runnable runnable) {
        this.keyValueStorage.run(runnable);
    }
}
