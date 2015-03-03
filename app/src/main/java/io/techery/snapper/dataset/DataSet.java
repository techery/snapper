package io.techery.snapper.dataset;

import java.util.ArrayList;
import java.util.List;

public abstract class DataSet<T> implements IDataSet<T> {

    private final List<Listener<T>> listeners = new ArrayList<Listener<T>>();

    @Override
    public void addListener(final Listener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final Listener<T> listener) {
        listeners.remove(listener);
    }

    protected void didUpdateDataSet(final DataSetChange<T> change) {
        for (final Listener<T> listener : listeners) {
            listener.onDataSetUpdated(this, change);
        }
    }
}
