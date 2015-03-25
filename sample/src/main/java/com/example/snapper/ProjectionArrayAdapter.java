package com.example.snapper;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.projection.IProjection;
import io.techery.snapper.util.android.MainThreadDataListener;

public class ProjectionArrayAdapter<T> extends ArrayAdapter<T> implements IDataSet.DataListener<T> {

    static final String TAG = ProjectionArrayAdapter.class.getSimpleName();

    private IProjection<T> projection;

    public ProjectionArrayAdapter(Context context, int resource, IProjection<T> projection) {
        super(context, resource);
        setProjection(projection);
    }

    public IProjection<T> getProjection() {
        return projection;
    }

    public void setProjection(IProjection<T> projection) {
        if (this.projection != null) {
            this.projection.removeDataListener(this);
        }

        this.projection = projection;
        this.projection.addDataListener(new MainThreadDataListener<T>(this));
    }

    @Override
    public void onDataUpdated(List<T> items, StorageChange<T> change) {
        syncWithProjection(items);
        notifyDataSetChanged();
    }

    private void syncWithProjection(List<T> items) {
        clear();
        addAll(items);
        Log.d(TAG, "Synced items: " + items.size());
    }

}
