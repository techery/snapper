package io.techery.snapper.helper;

import com.innahema.collections.query.queriables.Queryable;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;

import static io.techery.snapper.helper.SingleItemDataListener.ChangeStatus.ADDED;
import static io.techery.snapper.helper.SingleItemDataListener.ChangeStatus.DEFAULT;
import static io.techery.snapper.helper.SingleItemDataListener.ChangeStatus.REMOVED;
import static io.techery.snapper.helper.SingleItemDataListener.ChangeStatus.UPDATED;

public abstract class SingleItemDataListener<T> implements IDataSet.DataListener<T> {

    private T defaultItem;

    public enum ChangeStatus {
        DEFAULT, ADDED, UPDATED, REMOVED
    }

    public SingleItemDataListener() {
    }

    public SingleItemDataListener(T defaultItem) {
        this.defaultItem = defaultItem;
    }

    @Override public void onDataUpdated(final List<T> items, final StorageChange<T> change) {
        ChangeStatus status = change.getAdded().isEmpty() ?
                (change.getUpdated().isEmpty() ?
                        (change.getRemoved().isEmpty() ? DEFAULT : REMOVED)
                        : UPDATED)
                : ADDED;
        T item = Queryable.from(items).firstOrDefault();
        if (status == DEFAULT) {
            if (item == null) item = defaultItem;
            else status = ADDED;
        }
        onItemUpdated(item, status);
    }

    protected abstract void onItemUpdated(T t, ChangeStatus status);
}
