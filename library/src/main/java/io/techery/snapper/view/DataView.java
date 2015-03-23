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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public class DataView<T> extends DataSet<T> implements IDataView<T>, IDataSet.DataListener<T>, IDataSet.StatusListener {

    public static final String TAG = "DataView";

    private final List<ItemRef<T>> items;
    private final Set<ItemRef<T>> keys;
    private final Predicate<T> predicate;
    private final Comparator<T> comparator;
    private final Comparator<ItemRef<T>> itemComparator;
    private final WeakReference<IDataSet<T>> dataSetRef;
    private final ReadWriteLock lock;
    private boolean isClosed;

    DataView(final IDataSet<T> dataSet, Predicate<T> predicate, Comparator<T> comparator) {
        this.dataSetRef = new WeakReference<>(dataSet);
        this.comparator = comparator;
        this.predicate = predicate;
        this.items = new ArrayList<>();
        this.keys = new HashSet<>();
        this.lock = new ReentrantReadWriteLock();

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
                lock.writeLock().lock();
                keys.addAll(acceptedItems);
                items.addAll(acceptedItems);
                Collections.sort(items, itemComparator);
                lock.writeLock().unlock();

                didUpdateDataSet(StorageChange.buildWithAdded(items));

                IDataSet<T> parentDataSet = DataView.this.dataSetRef.get();
                if (parentDataSet != null) {
                    parentDataSet.addDataListener(DataView.this);
                    parentDataSet.addStatusListener(DataView.this);
                }
                Log.d(TAG, "Initialized with " + items.size() + " elements");
            }
        });
    }

    @Override public void onClosed() {
        close();
    }

    @Override
    public void close() {
        isClosed = true;
        IDataSet<T> parentDataSet = dataSetRef.get();
        if (parentDataSet != null) {
            parentDataSet.removeDataListener(this);
            parentDataSet.removeStatusListener(this);
        }
        didClose();
        lock.writeLock().lock();
        items.clear();
        lock.writeLock().unlock();
    }

    @Override public boolean isClosed() {
        return isClosed;
    }

    public Builder<T> view() {
        throwIfClosed();
        return new DataViewBuilder<T>(this).sort(this.comparator);
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        lock.readLock().lock();
        Iterator<ItemRef<T>> iterator = Queryable.from(items).toList().iterator();
        lock.readLock().unlock();
        return iterator;
    }

    @Override
    public T getItem(int index) {
        throwIfClosed();
        lock.readLock().lock();
        T value = items.get(index).getValue();
        lock.readLock().unlock();
        return value;
    }

    @Override
    public List<T> toList() {
        throwIfClosed();
        lock.readLock().lock();
        List<T> list = Queryable
                .from(items)
                .map(new Converter<ItemRef<T>, T>() {
                    @Override
                    public T convert(ItemRef<T> element) {
                        return element.getValue();
                    }
                }).toList();
        lock.readLock().unlock();
        return list;
    }

    @Override
    public int size() {
        throwIfClosed();
        lock.readLock().lock();
        int size = items.size();
        lock.readLock().unlock();
        return size;
    }

    @Override
    public void onDataUpdated(List<T> collection, StorageChange<T> change) {
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
                lock.writeLock().lock();
                this.items.add(item);
                lock.writeLock().unlock();
                addedItems.add(item);
            }
        }
        Collections.sort(items, itemComparator);

        Log.d(TAG + ":ADD", "finished with new:" + addedItems.size() + "; overall:" + this.items.size());
        return addedItems;
    }

    private List<ItemRef<T>> processRemovedItems(List<ItemRef<T>> removed) {
        Log.d(TAG + ":REMOVE", "started with size " + removed.size());
        List<ItemRef<T>> removedItems = new ArrayList<>();
        for (ItemRef<T> item : removed) {
            if (this.keys.remove(item)) {
                lock.writeLock().lock();
                this.items.remove(item);
                lock.writeLock().unlock();
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
                    lock.writeLock().lock();
                    this.items.remove(item);
                    lock.writeLock().unlock();
                    updatedItems.add(item);
                } else {
                    localAddedItems.add(item);
                }
                lock.writeLock().lock();
                this.items.add(item);
                lock.writeLock().unlock();
                hasNew = true;
            } else if (this.keys.remove(item)) {
                lock.writeLock().lock();
                this.items.remove(item);
                lock.writeLock().unlock();
                localRemovedItems.add(item);
            }
        }
        lock.writeLock().lock();
        if (hasNew) Collections.sort(items, itemComparator);
        lock.writeLock().unlock();

        Log.d(TAG + ":UPDATE", "finished with new:" + updatedItems.size() + "; overall:" + this.items.size());
        return updatedItems;
    }

    @Override
    public void perform(Runnable runnable) {
        IDataSet<T> dataSet = this.dataSetRef.get();
        if (dataSet != null) {
            dataSet.perform(runnable);
        }
    }

    private void throwIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("DataView is closed, no further operations permitted");
        }
    }
}
