package io.techery.snapper.listadapter;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.android.MainThreadDataListenerProxy;
import io.techery.snapper.view.IDataView;

public class DataViewListAdapter<T> extends ArrayAdapter<T> implements IDataSet.DataListener<T> {

    static final String TAG = DataViewListAdapter.class.getSimpleName();

    private IDataView<T> dataView;

    public DataViewListAdapter(Context context, int resource, IDataView<T> dataView) {
        super(context, resource);
        setDataView(dataView);
    }

    public IDataView<T> getDataView() {
        return dataView;
    }

    public void setDataView(IDataView<T> dataView) {
        if (this.dataView != null) {
            this.dataView.removeDataListener(this);
        }

        this.dataView = dataView;
        this.dataView.addDataListener(new MainThreadDataListenerProxy<T>(this));
    }

    @Override
    public void onDataUpdated(StorageChange<T> change) {
        syncWithDataView(dataView.toList());
        notifyDataSetChanged();
    }

    private void syncWithDataView(List<T> items) {
        clear();
        addAll(items);
        Log.d(TAG, "Synced items: " + items.size());
    }

}
