package io.techery.snapper.dataset;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.queriables.Queryable;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public class DataSetMap<F, T> implements IDataSet<T>, IDataSet.DataListener<F> {

    private final IDataSet<F> originalDataSet;
    private final Function1<F, T> mapFunction;
    private final List<DataListener<T>> listeners = new CopyOnWriteArrayList<DataListener<T>>();

    public DataSetMap(IDataSet<F> originalDataSet, Function1<F, T> mapFunction) {
        this.originalDataSet = originalDataSet;
        this.mapFunction = mapFunction;
        this.originalDataSet.addDataListener(this);
    }

    @Override public void close() {
        throw new UnsupportedOperationException("Transmitter set couldn't be closed directly");
    }

    @Override public boolean isClosed() {
        return originalDataSet.isClosed();
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        final Iterator<ItemRef<F>> iterator = this.originalDataSet.iterator();
        return new Iterator<ItemRef<T>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ItemRef<T> next() {
                final ItemRef<F> itemRef = iterator.next();
                return new ItemRef<>(itemRef.getKey(), mapFunction.apply(itemRef.getValue()));
            }

            @Override
            public void remove() {

            }
        };
    }

    @Override
    public void addDataListener(final DataListener<T> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeDataListener(DataListener<T> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void addStatusListener(StatusListener listener) {
        originalDataSet.addStatusListener(listener);
    }

    @Override
    public void removeStatusListener(StatusListener listener) {
        originalDataSet.removeStatusListener(listener);
    }

    @Override
    public void perform(Runnable runnable) {
        this.originalDataSet.perform(runnable);
    }

    @Override
    public void onDataUpdated(IDataSet<F> dataSet, StorageChange<F> change) {
        final StorageChange<T> mappedChange = mapChange(change);
        for (DataListener<T> listener : listeners) {
            listener.onDataUpdated(this, mappedChange);
        }
    }

    private StorageChange<T> mapChange(StorageChange<F> change) {
        Converter<ItemRef<F>, ItemRef<T>> converter = new Converter<ItemRef<F>, ItemRef<T>>() {
            @Override
            public ItemRef<T> convert(ItemRef<F> element) {
                return new ItemRef<T>(element.getKey(), mapFunction.apply(element.getValue()));
            }
        };

        List<ItemRef<T>> added = Queryable.from(change.getAdded()).map(converter).toList();
        List<ItemRef<T>> removed = Queryable.from(change.getRemoved()).map(converter).toList();
        List<ItemRef<T>> updated = Queryable.from(change.getUpdated()).map(converter).toList();

        return new StorageChange<T>(added, updated, removed);
    }
}
