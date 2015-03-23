package io.techery.snapper.dataset;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.snapper.storage.StorageChange;

public abstract class DataSet<T> implements IDataSet<T> {

    ///////////////////////////////////////////////////////////////////////////
    // Data
    ///////////////////////////////////////////////////////////////////////////

    private final List<DataListener<T>> dataListeners = new CopyOnWriteArrayList<DataListener<T>>();

    @Override
    public void addDataListener(final DataListener<T> listener) {
        dataListeners.add(listener);
        perform(new Runnable() {
            @Override
            public void run() {
                listener.onDataUpdated(StorageChange.<T>empty());
            }
        });
    }

    @Override
    public void removeDataListener(final DataListener<T> listener) {
        dataListeners.remove(listener);
    }

    protected void didUpdateDataSet(final StorageChange<T> change) {
        for (DataListener<T> listener : dataListeners) {
            listener.onDataUpdated(change);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Status
    ///////////////////////////////////////////////////////////////////////////

    private final List<StatusListener> statusListeners = new CopyOnWriteArrayList<StatusListener>();

    @Override public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    @Override public void removeStatusListener(StatusListener listener) {

    }

    protected void didClose() {
        for (StatusListener listener : statusListeners) {
            listener.onClosed();
        }
    }

}
