package io.techery.snapper.projection;

import com.innahema.collections.query.functions.Predicate;

import java.util.Comparator;

import io.techery.snapper.dataset.IDataSet;

public interface IProjection<T> extends IDataSet<T> {

    Builder<T> projection();
    T getItem(int index);

    interface Builder<T> {
        Builder<T> where(Predicate<T> predicate);
        Builder<T> sort(Comparator<T> comparator);
        IProjection<T> build();
    }
}
