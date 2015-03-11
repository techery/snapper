package io.techery.snapper.listadapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.view.IDataView;
import timber.log.Timber;

public class DataViewListAdapter<T> extends ArrayAdapter<T> implements IDataSet.Listener<T> {

    private IDataView<T> dataView;
    private final Handler handler;

    public DataViewListAdapter(Context context, int resource, IDataView<T> dataView) {
        super(context, resource);
        setDataView(dataView);
        handler = new Handler(Looper.getMainLooper());
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                syncWithDataView();
                notifyDataSetChanged();
            }
        });
    }

    private void syncWithDataView() {
        Timber.i("Synced with data set");
        clear();
        if (dataView != null) {
            List<T> collection = dataView.toList();
            addAll(collection);
            Timber.d("Data added %d", collection.size());
        }
    }
}
