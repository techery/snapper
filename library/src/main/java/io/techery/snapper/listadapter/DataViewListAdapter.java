package io.techery.snapper.listadapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.view.IDataView;

public class DataViewListAdapter<T> extends ArrayAdapter<T> implements IDataSet.Listener<T> {

    static final String TAG = DataViewListAdapter.class.getSimpleName();

    private IDataView<T> dataView;
    private final Handler handler;

    public DataViewListAdapter(Context context, int resource, IDataView<T> dataView) {
        super(context, resource);
        handler = new Handler(Looper.getMainLooper());
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
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                syncWithDataView();
                notifyDataSetChanged();
            }
        }, 50);
    }

    private void syncWithDataView() {
        Log.i(TAG, "Synced with data set");
        clear();
        if (dataView != null) {
            List<T> collection = dataView.toList();
            addAll(collection);
            Log.i(TAG, "Data added " + collection.size());
        }
    }
}
