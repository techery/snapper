package com.example.snapper.rx.util;

import android.util.Pair;

import java.util.Collections;
import java.util.List;

import io.techery.snapper.model.Indexable;
import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;
import rx.functions.Func2;

public class SnapperChangeSort<T extends Indexable> implements Transformer<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> {

    private final Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> comparator;

    public SnapperChangeSort(Func2<T, T, Integer> comparator) {
        this.comparator = createComparator(comparator);
    }

    @Override
    public Observable<Pair<List<T>, StorageChange<T>>> call(Observable<Pair<List<T>, StorageChange<T>>> source) {
        return source.map(this.comparator::call);

    }

    private Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> createComparator(Func2<T, T, Integer> comparator) {
        return pair -> {
            List<T> list = Observable.from(pair.first).toSortedList(comparator).toBlocking().first();
            List<ItemRef<T>> added = Collections.emptyList();
            List<ItemRef<T>> removed = Collections.emptyList();
            List<ItemRef<T>> updated = Observable.from(list).map(i -> ItemRef.make(i)).toList().toBlocking().first();

            return new Pair<>(list, new StorageChange<>(added, updated, removed));
        };
    }

    public Func1<Pair<List<T>, StorageChange<T>>, Pair<List<T>, StorageChange<T>>> getComparator() {
        return comparator;
    }
}
