package io.techery.snapper.util.android;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;

public class MainThreadListenerProxy<T> implements IDataSet.Listener<T> {

    private final Handler handler;
    private final WeakReference<IDataSet.Listener<T>> listenerRef;

    public MainThreadListenerProxy(IDataSet.Listener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener may not be null");
        }
        listenerRef = new WeakReference<IDataSet.Listener<T>>(listener);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public void onDataSetUpdated(final IDataSet<T> dataSet, final StorageChange<T> change) {
        handler.post(new Runnable() {
            @Override public void run() {
                IDataSet.Listener<T> listener = listenerRef.get();
                if (listener != null) {
                    listener.onDataSetUpdated(dataSet, change);
                }
            }
        });
    }
}
