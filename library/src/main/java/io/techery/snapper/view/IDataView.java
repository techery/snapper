package io.techery.snapper.view;

import com.innahema.collections.query.functions.Predicate;

import java.util.Comparator;
import java.util.List;

import io.techery.snapper.dataset.IDataSet;

public interface IDataView<T> extends IDataSet<T> {

    public Builder<T> view();

    void close();

    int size();

    T getItem(int index);

    List<T> toList();

    public interface Builder<T> {
        public Builder<T> where(Predicate<T> predicate);

        Builder<T> sort(Comparator<T> comparator);

        IDataView<T> build();
    }
}
