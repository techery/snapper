package com.example.snapper.rx.util;

import android.util.Pair;

import java.util.List;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

public class SnapperChangeMapper<T extends Indexable, R> implements Transformer<Pair<List<T>, StorageChange<T>>, Pair<List<R>, StorageChange<R>>> {

    private final Func1<T, R> func;

    public SnapperChangeMapper(Func1<T, R> func) {
        this.func = func;
    }

    @Override
    public Observable<Pair<List<R>, StorageChange<R>>> call(Observable<Pair<List<T>, StorageChange<T>>> source) {
        return source.map(pair -> {
            List<R> list = Observable.from(pair.first).map(func).toList().toBlocking().first();
            List<ItemRef<R>> added = Observable.from(pair.second.getAdded())
                    .map((ItemRef<T> itemRef) -> new ItemRef<>(itemRef.getKey(), func.call(itemRef.getValue())))
                    .toList()
                    .toBlocking()
                    .first();
            List<ItemRef<R>> updated = Observable.from(pair.second.getUpdated())
                    .map((ItemRef<T> itemRef) -> new ItemRef<>(itemRef.getKey(), func.call(itemRef.getValue())))
                    .toList()
                    .toBlocking()
                    .first();
            List<ItemRef<R>> removed = Observable.from(pair.second.getRemoved())
                    .map((ItemRef<T> itemRef) -> new ItemRef<>(itemRef.getKey(), func.call(itemRef.getValue())))
                    .toList()
                    .toBlocking()
                    .first();
            return new Pair<>(list, new StorageChange<>(added, updated, removed));
        });
    }
}
