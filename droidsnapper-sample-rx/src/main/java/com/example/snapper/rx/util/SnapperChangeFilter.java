package com.example.snapper.rx.util;

import android.util.Pair;

import java.util.List;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

public class SnapperChangeFilter<T extends Indexable> implements Transformer<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> {

    private final Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> filter;

    public SnapperChangeFilter(Func1<T, Boolean> predicate) {
        this.filter = createFilter(predicate);
    }

    @Override
    public Observable<Pair<List<T>, StorageChange<T>>> call(Observable<Pair<List<T>, StorageChange<T>>> source) {
        return source.map(this.filter::call);

    }

    private Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> createFilter(Func1<T, Boolean> predicate) {
        return pair -> {
            List<T> result = Observable.from(pair.first).filter(predicate).toList().toBlocking().first();
            StorageChange<T> storageChange;
            List<ItemRef<T>> added = Observable.from(pair.second.getAdded())
                    .filter(itemRef -> predicate.call(itemRef.getValue()))
                    .toList()
                    .toBlocking()
                    .first();
            List<ItemRef<T>> updated = Observable.from(pair.second.getUpdated())
                    .filter(itemRef -> predicate.call(itemRef.getValue()))
                    .toList()
                    .toBlocking()
                    .first();
            List<ItemRef<T>> removed = Observable.from(pair.second.getRemoved())
                    .filter(itemRef -> predicate.call(itemRef.getValue()))
                    .toList()
                    .toBlocking()
                    .first();
            //
            storageChange = new StorageChange<>(added, updated, removed);
            return new Pair<>(result, storageChange);
        };
    }

    public Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> getFilter() {
        return filter;
    }
}
