package io.techery.snapper.dataset;

import io.techery.snapper.model.ItemRef;

public interface IDataSet<T> extends Iterable<ItemRef<T>> {

    public interface Listener<T> {
        void onDataSetUpdated(IDataSet<T> dataSet, DataSetChange<T> change);
    }

    public void addListener(Listener<T> listener);

    public void removeListener(Listener<T> listener);

    public void run(Runnable runnable);
}
