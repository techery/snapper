package com.example.snapper.rx;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import io.techery.rxlistadapter.RxAdapterBridge;
import io.techery.rxlistadapter.RxListAdapter;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.Subscriber;

public abstract class SnapperRxListAdapter<T, VH extends RecyclerView.ViewHolder> extends RxListAdapter<T, Pair<List<T>, StorageChange<T>>, VH> {

    public SnapperRxListAdapter(Context context, Observable<Pair<List<T>, StorageChange<T>>> source) {
        super(context, new ArrayList<>(), source);
    }

    @Override
    public RxAdapterBridge<T, Pair<List<T>, StorageChange<T>>> createRxAdapterBridge(List<T> items, Observable<Pair<List<T>, StorageChange<T>>> source) {
        return new RxChangeAdapterBridge<T>(this, source, items);
    }

    private static class RxChangeAdapterBridge<T> extends RxAdapterBridge<T, Pair<List<T>, StorageChange<T>>> {

        public RxChangeAdapterBridge(RecyclerView.Adapter adapter, Observable<Pair<List<T>, StorageChange<T>>> source, List<T> items) {
            super(adapter, source, items);
        }

        @Override protected Subscriber<Pair<List<T>, StorageChange<T>>> subscriber() {
            return new Subscriber<Pair<List<T>, StorageChange<T>>>() {
                @Override public void onNext(Pair<List<T>, StorageChange<T>> ts) {
                    List<T> newItems = ts.first;
                    StorageChange<T> storageChange = ts.second;
                    if (itemsEquals(newItems, storageChange)) return;
                    //
                    if (items.isEmpty()) {
                        setNewItems(newItems);
                    } else {
                        for (ItemRef<T> item : storageChange.getAdded()) {
                            onAdded(newItems, item.getValue());
                        }
                        for (ItemRef<T> item : storageChange.getRemoved()) {
                            onRemoved(item.getValue());
                        }
                        for (ItemRef<T> item : storageChange.getUpdated()) {
                            onUpdated(newItems, item.getValue());
                        }
                    }
                    if (items.size() != newItems.size()) setNewItems(newItems);
                }

                private boolean itemsEquals(List<T> newItems, StorageChange<T> storageChange) {
                    return items.equals(newItems) && storageChange.getUpdated().size() == 0;
                }

                private void setNewItems(List<T> newItems) {
                    items.clear();
                    items.addAll(newItems);
                    adapter.notifyDataSetChanged();
                }

                private void onAdded(List<T> newItems, T value) {
                    if (items.contains(value)) return;
                    int i = newItems.indexOf(value);
                    items.add(i, value);
                    adapter.notifyItemInserted(i);
                }

                private void onRemoved(T item) {
                    int pos = items.indexOf(item);
                    if (pos == -1) return;
                    items.remove(item);
                    adapter.notifyItemRemoved(pos);
                }

                private void onUpdated(List<T> newItems, T item) {
                    int oldPos = items.indexOf(item);
                    if (oldPos == -1) return;
                    int newPos = newItems.indexOf(item);
                    if (oldPos == newPos) {
                        items.set(oldPos, item);
                        adapter.notifyItemChanged(oldPos);
                    } else {
                        items.remove(oldPos);
                        if (newPos > items.size()) newPos = items.size();
                        items.add(newPos, item);
                        adapter.notifyItemMoved(oldPos, newPos);
                    }
                }

                @Override public void onCompleted() {
                }

                @Override public void onError(Throwable e) {
                }
            };
        }
    }
}
