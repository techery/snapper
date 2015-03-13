package io.techery.snapper.dataset;

import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public interface IDataSet<T> extends Iterable<ItemRef<T>> {

    public interface Listener<T> {
        void onDataSetUpdated(IDataSet<T> dataSet, StorageChange<T> change);
    }

    public void addListener(Listener<T> listener);

    public void removeListener(Listener<T> listener);

    public void perform(Runnable runnable);
}
