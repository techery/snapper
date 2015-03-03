package io.techery.snapper.dataset;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function1;
import com.innahema.collections.query.queriables.Queryable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.techery.snapper.model.ItemRef;

public class DataSetMap<F, T> implements IDataSet<T>, IDataSet.Listener<F> {

    private final IDataSet<F> originalDataSet;
    private final Function1<F, T> mapFunction;
    private final List<Listener<T>> listeners = new ArrayList<Listener<T>>();

    public DataSetMap(IDataSet<F> originalDataSet, Function1<F, T> mapFunction) {
        this.originalDataSet = originalDataSet;
        this.mapFunction = mapFunction;
        this.originalDataSet.addListener(this);
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
    public void addListener(final Listener<T> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(Listener<T> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void run(Runnable runnable) {
        this.originalDataSet.run(runnable);
    }

    @Override
    public void onDataSetUpdated(IDataSet<F> dataSet, DataSetChange<F> change) {
        final DataSetChange<T> mappedChange = mapChange(change);
        for (Listener<T> listener : listeners) {
            listener.onDataSetUpdated(this, mappedChange);
        }
    }

    private DataSetChange<T> mapChange(DataSetChange<F> change) {

        final Converter<ItemRef<F>, ItemRef<T>> converter = new Converter<ItemRef<F>, ItemRef<T>>() {
            @Override
            public ItemRef<T> convert(ItemRef<F> element) {
                return new ItemRef<T>(element.getKey(), mapFunction.apply(element.getValue()));
            }
        };

        final List<ItemRef<T>> added = Queryable.from(change.getAdded()).map(converter).toList();
        final List<ItemRef<T>> removed = Queryable.from(change.getRemoved()).map(converter).toList();
        final List<ItemRef<T>> updated = Queryable.from(change.getUpdated()).map(converter).toList();

        return new DataSetChange<T>(added, updated, removed);
    }
}
