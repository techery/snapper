package io.techery.snapper.view;

import com.innahema.collections.query.functions.Predicate;

import java.util.Comparator;

import io.techery.snapper.dataset.IDataSet;

public interface IDataView<T> extends IDataSet<T> {

    Builder<T> view();
    T getItem(int index);

    interface Builder<T> {
        Builder<T> where(Predicate<T> predicate);
        Builder<T> sort(Comparator<T> comparator);
        IDataView<T> build();
    }
}
