package techery.io.snappytest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import io.techery.snapper.dataset.DataSetChange;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.view.IDataView;

public class DataViewListAdapter<T> extends ArrayAdapter<T> implements IDataSet.Listener<T> {

    private IDataView<T> dataView;

    public DataViewListAdapter(Context context, int resource, IDataView<T> dataView) {
        super(context, resource);
        this.dataView = dataView;
        this.dataView.addListener(this);
        syncWithDataView();
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

        syncWithDataView();
    }

    @Override
    public void onDataSetUpdated(final IDataSet<T> dataSet, DataSetChange<T> change) {

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
            addAll(dataView.toList());
        }
    }
}
