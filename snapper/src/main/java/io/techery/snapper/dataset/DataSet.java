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
        if (isInitialized()) {
            perform(new Runnable() {
                @Override
                public void run() {
                    if (dataListeners.contains(listener)) {
                        listener.onDataUpdated(toList(), StorageChange.<T>empty());
                    }
                }
            });
        }
    }

    @Override
    public void removeDataListener(final DataListener<T> listener) {
        dataListeners.remove(listener);
    }

    protected void clearDataListeners() {
        dataListeners.clear();
    }

    protected void didUpdateDataSet(final StorageChange<T> change) {
        if (isClosed()) return;
        List<T> items = toList();
        for (DataListener<T> listener : dataListeners) {
            listener.onDataUpdated(items, change);
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
        statusListeners.remove(listener);
    }

    protected void clearStatusListeners() {
        statusListeners.clear();
    }

    protected void didClose() {
        for (StatusListener listener : statusListeners) {
            listener.onClosed();
        }
    }

}
