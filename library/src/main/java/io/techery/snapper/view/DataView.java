package io.techery.snapper.view;

import android.util.Log;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public class DataView<T> extends DataSet<T> implements IDataView<T>, IDataSet.Listener<T> {

    public static final String TAG = "DataView";

    private final List<ItemRef<T>> items;
    private final Set<ItemRef<T>> keys;
    private final Predicate<T> predicate;
    private final Comparator<T> comparator;
    private final Comparator<ItemRef<T>> itemComparator;
    private final WeakReference<IDataSet<T>> dataSetRef;
    private final ReentrantLock lock;

    DataView(final IDataSet<T> dataSet, Predicate<T> predicate, Comparator<T> comparator) {
        this.dataSetRef = new WeakReference<>(dataSet);
        this.comparator = comparator;
        this.predicate = predicate;
        this.items = new ArrayList<>();
        this.keys = new HashSet<>();
        this.lock = new ReentrantLock();

        this.itemComparator = new Comparator<ItemRef<T>>() {
            @Override
            public int compare(ItemRef<T> o1, ItemRef<T> o2) {
                return DataView.this.comparator.compare(o1.getValue(), o2.getValue());
            }
        };

        initializeItems(dataSet);
    }

    private void initializeItems(final IDataSet<T> parentDataSet) {
        parentDataSet.perform(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Initializing");
                List<ItemRef<T>> acceptedItems = Queryable.from(parentDataSet).where(new Predicate<ItemRef<T>>() {
                    @Override public boolean apply(ItemRef<T> element) {
                        return DataView.this.predicate.apply(element.getValue());
                    }
                }).toList();
                lock.lock();
                keys.addAll(acceptedItems);
                items.addAll(acceptedItems);
                Collections.sort(items, itemComparator);
                lock.unlock();

                didUpdateDataSet(StorageChange.buildWithAdded(items));

                IDataSet<T> parentDataSet = DataView.this.dataSetRef.get();
                if (parentDataSet != null) {
                    parentDataSet.addListener(DataView.this);
                }
                Log.d(TAG, "Initialized with " + items.size() + " elements");
            }
        });
    }

    @Override
    public void close() {
        IDataSet<T> parentDataSet = DataView.this.dataSetRef.get();
        if (parentDataSet != null) {
            parentDataSet.removeListener(DataView.this);
        }
        clearListeners();
        lock.lock();
        items.clear();
        lock.unlock();
    }

    public Builder<T> view() {
        return new DataViewBuilder<T>(this).sort(this.comparator);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public T getItem(int index) {
        lock.lock();
        T value = items.get(index).getValue();
        lock.unlock();
        return value;
    }

    @Override
    public List<T> toList() {
        lock.lock();
        List<T> list = Queryable
                .from(items)
                .map(new Converter<ItemRef<T>, T>() {
                    @Override
                    public T convert(ItemRef<T> element) {
                        return element.getValue();
                    }
                }).toList();
        lock.unlock();
        return list;
    }

    @Override
    public void onDataSetUpdated(IDataSet<T> dataSet, StorageChange<T> change) {
        didUpdateDataSet(processChange(change));
    }

    private StorageChange<T> processChange(StorageChange<T> change) {
        List<ItemRef<T>> addedItems = processAddedItems(change.getAdded());
        List<ItemRef<T>> removedItems = processRemovedItems(change.getRemoved());
        List<ItemRef<T>> updatedItems = processUpdatedItems(change.getUpdated(), addedItems, removedItems);

        return new StorageChange<>(addedItems, updatedItems, removedItems);
    }

    private List<ItemRef<T>> processAddedItems(List<ItemRef<T>> added) {
        Log.d(TAG + ":ADD", "started with size " + added.size());
        List<ItemRef<T>> addedItems = new ArrayList<>();

        for (ItemRef<T> item : added) {
            if (predicate.apply(item.getValue()) && this.keys.add(item)) {
                lock.lock();
                this.items.add(item);
                lock.unlock();
                addedItems.add(item);
            }
        }
        Collections.sort(items, itemComparator);

        Log.d(TAG + ":ADD", "finished with new:" + addedItems.size() + "; overall:" + this.items.size());
        return addedItems;
    }

    private List<ItemRef<T>> processRemovedItems(List<ItemRef<T>> removed) {
        Log.d(TAG + ":REMOVE", "started with size " + removed.size());
        long start = System.currentTimeMillis();
        List<ItemRef<T>> removedItems = new ArrayList<>();
        for (ItemRef<T> item : removed) {
            if (this.keys.remove(item)) {
                lock.lock();
                this.items.remove(item);
                lock.unlock();
                removedItems.add(item);
            }
        }
        Log.d(TAG + ":REMOVE", "finished with new:" + removedItems.size() + "; overall:" + this.items.size());
        return removedItems;
    }

    private List<ItemRef<T>> processUpdatedItems(List<ItemRef<T>> updated, List<ItemRef<T>> localAddedItems, List<ItemRef<T>> localRemovedItems) {
        Log.d(TAG + ":UPDATE", "started with size " + updated.size());
        List<ItemRef<T>> updatedItems = new ArrayList<>();

        boolean hasNew = false;
        for (ItemRef<T> item : updated) {
            if (this.predicate.apply(item.getValue())) {
                if (this.keys.contains(item)) {
                    lock.lock();
                    this.items.remove(item);
                    lock.unlock();
                    updatedItems.add(item);
                } else {
                    localAddedItems.add(item);
                }
                lock.lock();
                this.items.add(item);
                lock.unlock();
                hasNew = true;
            } else if (this.keys.remove(item)) {
                lock.lock();
                this.items.remove(item);
                lock.unlock();
                localRemovedItems.add(item);
            }
        }
        lock.lock();
        if (hasNew) Collections.sort(items, itemComparator);
        lock.unlock();

        Log.d(TAG + ":UPDATE", "finished with new:" + updatedItems.size() + "; overall:" + this.items.size());
        return updatedItems;
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return items.iterator();
    }

    @Override
    public void perform(Runnable runnable) {
        IDataSet<T> dataSet = this.dataSetRef.get();
        if (dataSet != null) {
            dataSet.perform(runnable);
        }
    }

}
