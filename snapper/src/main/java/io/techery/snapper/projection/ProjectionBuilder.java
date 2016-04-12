package io.techery.snapper.projection;

import com.innahema.collections.query.functions.Predicate;

import java.util.Comparator;

import io.techery.snapper.dataset.IDataSet;

public class ProjectionBuilder<T> implements IProjection.Builder<T> {

    private final IDataSet<T> parentDataSet;

    public ProjectionBuilder(IDataSet<T> dataSet) {
        this.parentDataSet = dataSet;
    }

    private Predicate<T> predicate = DEFAULT_PREDICATE;
    private Comparator<T> comparator = DEFAULT_COMPARATOR;

    public IProjection.Builder<T> where(Predicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public IProjection.Builder<T> sort(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    public IProjection<T> build() {
        return new Projection<T>(this.parentDataSet, this.predicate, this.comparator);
    }

    public static final Predicate DEFAULT_PREDICATE = new Predicate() {
        @Override
        public boolean apply(Object element) {
            return true;
        }
    };

    private static final Comparator DEFAULT_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return 0;
        }
    };
}