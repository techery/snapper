package io.techery.snapper.view;

import com.innahema.collections.query.functions.Predicate;

import java.util.Comparator;

import io.techery.snapper.dataset.IDataSet;

public class DataViewBuilder<T> implements IDataView.Builder<T> {

    private final IDataSet<T> parentDataSet;

    public DataViewBuilder(IDataSet<T> dataSet) {
        this.parentDataSet = dataSet;
    }

    private Predicate<T> predicate = DEFAULT_PREDICATE;
    private Comparator<T> comparator = DEFAULT_COMPARATOR;

    public IDataView.Builder<T> where(Predicate<T> predicate) {
        this.predicate = predicate;
        return this;
    }

    public IDataView.Builder<T> sort(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    public IDataView<T> build() {
        return new DataView<T>(this.parentDataSet, this.predicate, this.comparator);
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