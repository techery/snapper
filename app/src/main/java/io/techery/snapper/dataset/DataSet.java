package io.techery.snapper.dataset;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.snapper.storage.StorageChange;

public abstract class DataSet<T> implements IDataSet<T> {

    private final List<Listener<T>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(final Listener<T> listener) {
        listeners.add(listener);

        perform(new Runnable() {
            @Override
            public void run() {
                listener.onDataSetUpdated(DataSet.this, StorageChange.<T>empty());
            }
        });
    }

    @Override
    public void removeListener(final Listener<T> listener) {
        listeners.remove(listener);
    }

    protected void didUpdateDataSet(final StorageChange<T> change) {
        for (final Listener<T> listener : listeners) {
            listener.onDataSetUpdated(this, change);
        }
    }
}
