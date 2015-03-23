package io.techery.snapper.util.android;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;

public class MainThreadDataListenerProxy<T> implements IDataSet.DataListener<T> {

    private final Handler handler;
    private final WeakReference<IDataSet.DataListener<T>> listenerRef;

    public MainThreadDataListenerProxy(IDataSet.DataListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener may not be null");
        }
        listenerRef = new WeakReference<IDataSet.DataListener<T>>(listener);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override public void onDataUpdated(final List<T> items, final StorageChange<T> change) {
        handler.post(new Runnable() {
            @Override public void run() {
                IDataSet.DataListener<T> listener = listenerRef.get();
                if (listener != null) {
                    listener.onDataUpdated(items, change);
                }
            }
        });
    }
}
