package io.techery.snapper.dataset;

import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public interface IDataSet<T> extends Iterable<ItemRef<T>> {

    interface DataListener<T> {
        void onDataUpdated(IDataSet<T> dataSet, StorageChange<T> change);
    }

    void addDataListener(DataListener<T> listener);

    void removeDataListener(DataListener<T> listener);

    interface StatusListener {
        void onClosed();
    }

    void addStatusListener(StatusListener listener);

    void removeStatusListener(StatusListener listener);

    void close();

    boolean isClosed();

    void perform(Runnable runnable);
}
