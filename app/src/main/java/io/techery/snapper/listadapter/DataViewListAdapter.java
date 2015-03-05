package io.techery.snapper.listadapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.view.IDataView;

public class DataViewListAdapter<T> extends ArrayAdapter<T> implements IDataSet.Listener<T> {

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
            this.dataView.removeListener(this);
        }

        this.dataView = dataView;
        this.dataView.addListener(this);
    }

    @Override
    public void onDataSetUpdated(final IDataSet<T> dataSet, StorageChange<T> change) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                syncWithDataView();
                notifyDataSetChanged();
            }
        });
    }

    private void syncWithDataView() {
        clear();
        if (dataView != null) {
            Log.d("Adapter", "Add:" + dataView.size());
            addAll(dataView.toList());
        }
    }
}
