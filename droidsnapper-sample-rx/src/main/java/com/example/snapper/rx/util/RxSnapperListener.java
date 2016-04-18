package com.example.snapper.rx.util;

import android.util.Pair;

import java.util.List;

import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.storage.StorageChange;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

public class RxSnapperListener<T> implements IDataSet.DataListener<T>, IDataSet.StatusListener {

    private final ReplaySubject<Pair<List<T>, StorageChange<T>>> subject;
    private final String tag;

    public RxSnapperListener() {
        this(null);
    }

    public RxSnapperListener(String tag) {
        this.subject = ReplaySubject.createWithSize(1);
        this.tag = tag;
    }

    @Override public void onDataUpdated(List<T> list, StorageChange<T> storageChange) {
        subject.onNext(new Pair<>(list, storageChange));
    }

    @Override public void onClosed() {
        subject.onCompleted();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////

    public Observable<Pair<List<T>, StorageChange<T>>> observable() {
        return subject.asObservable();
    }

    public Observable<List<T>> listObservable() {
        return subject.map(new Func1<Pair<List<T>, StorageChange<T>>, List<T>>() {
            @Override public List<T> call(Pair<List<T>, StorageChange<T>> pair) {
                return pair.first;
            }
        });
    }

    public Observable<StorageChange<T>> changeObservable() {
        return subject.map(new Func1<Pair<List<T>, StorageChange<T>>, StorageChange<T>>() {
            @Override public StorageChange<T> call(Pair<List<T>, StorageChange<T>> pair) {
                return pair.second;
            }
        });
    }
}
