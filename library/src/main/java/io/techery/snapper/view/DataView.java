package io.techery.snapper.view;

import android.util.Log;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public class DataView<T> extends DataSet<T> implements IDataView<T>, IDataSet.Listener<T> {

    protected final List<ItemRef<T>> items = new CopyOnWriteArrayList<>();
    private Predicate<T> predicate;
    private Comparator<T> comparator;
    private final WeakReference<IDataSet<T>> dataSetWeakReference;

    private List<ItemRef<T>> build(IDataSet<T> dataSet) {
        return Queryable
                .from(dataSet)
                .where(new Predicate<ItemRef<T>>() {
                    @Override
                    public boolean apply(ItemRef<T> element) {
                        return DataView.this.predicate.apply(element.getValue());
                    }
                })
                .sort(getItemComparator())
                .toList();
    }

    private Comparator<ItemRef<T>> getItemComparator() {
        return new Comparator<ItemRef<T>>() {
            @Override
            public int compare(ItemRef<T> o1, ItemRef<T> o2) {
                return DataView.this.comparator.compare(o1.getValue(), o2.getValue());
            }
        };
    }

    DataView(final IDataSet<T> dataSet, Predicate<T> predicate, Comparator<T> comparator) {
        this.comparator = comparator;
        this.predicate = predicate;
        this.dataSetWeakReference = new WeakReference<>(dataSet);

        dataSet.perform(new Runnable() {
            @Override
            public void run() {
                items.addAll(build(dataSet));
                didUpdateDataSet(StorageChange.buildWithAdded(items));

                IDataSet<T> parentDataSet = DataView.this.dataSetWeakReference.get();
                if (parentDataSet != null) {
                    parentDataSet.addListener(DataView.this);
                }
            }
        });
    }

    @Override
    public void close() {
        IDataSet<T> parentDataSet = DataView.this.dataSetWeakReference.get();
        if (parentDataSet != null) {
            parentDataSet.removeListener(DataView.this);
        }
        clearListeners();
        items.clear();
    }

    public Builder<T> view() {
        return new DataViewBuilder<T>(this).sort(this.comparator);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public T getItem(int index) {
        return this.items.get(index).getValue();
    }

    @Override
    public List<T> toList() {
        return Queryable
                .from(items)
                .map(new Converter<ItemRef<T>, T>() {
                    @Override
                    public T convert(ItemRef<T> element) {
                        return element.getValue();
                    }
                }).toList();
    }

    private int indexForNewItem(ItemRef<T> item) {
        int index = Collections.binarySearch(this.items, item, getItemComparator());
        return index >= 0 ? index : -index - 1;
    }

    @Override
    public void onDataSetUpdated(IDataSet<T> dataSet, StorageChange<T> change) {
        didUpdateDataSet(processChange(change));
    }

    private StorageChange<T> processChange(StorageChange<T> change) {
        List<ItemRef<T>> addedItems = processAddedItems(change.getAdded());
        List<ItemRef<T>> removedItems = processRemovedItems(change.getRemoved());
        List<ItemRef<T>> updatedItems = processUpdatedItems(change.getUpdated(), removedItems);

        return new StorageChange<>(addedItems, updatedItems, removedItems);
    }

    private List<ItemRef<T>> processRemovedItems(List<ItemRef<T>> removed) {
        List<ItemRef<T>> removedItems = new ArrayList<>();
        for (ItemRef<T> item : removed) {
            if (this.items.contains(item)) {
                this.items.remove(item);
                removedItems.add(item);
            }
        }
        Log.i("DataView", "DataView Removed:" + removedItems.size() + "; Items:" + this.items.size());

        return removedItems;
    }

    private List<ItemRef<T>> processUpdatedItems(List<ItemRef<T>> updated, List<ItemRef<T>> localRemovedItems) {
        List<ItemRef<T>> updatedItems = new ArrayList<>();

        for (ItemRef<T> item : updated) {
            if (this.items.contains(item)) {
                if (this.predicate.apply(item.getValue())) {
                    this.items.remove(item);
                    int index = indexForNewItem(item);
                    this.items.add(index, item);
                    updatedItems.add(item);
                } else {
                    this.items.remove(item);
                    localRemovedItems.add(item);
                }
            }
        }
        Log.i("DataView", "DataView Updated:" + updatedItems.size() + "; Items:" + this.items.size());

        return updatedItems;
    }

    private List<ItemRef<T>> processAddedItems(List<ItemRef<T>> added) {
        List<ItemRef<T>> addedItems = new ArrayList<>();

        for (ItemRef<T> item : added) {
            if (this.predicate.apply(item.getValue())) {
                int index = indexForNewItem(item);
                this.items.add(index, item);
                addedItems.add(item);
            }
        }
        Log.i("DataView", "DataView Added:" + addedItems.size() + "; Items:" + this.items.size());

        return addedItems;
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return items.iterator();
    }

    @Override
    public void perform(Runnable runnable) {
        IDataSet<T> dataSet = this.dataSetWeakReference.get();
        if (dataSet != null) {
            dataSet.perform(runnable);
        }
    }
}
