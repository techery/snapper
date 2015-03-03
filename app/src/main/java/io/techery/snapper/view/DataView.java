package io.techery.snapper.view;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import io.techery.snapper.dataset.DataSet;
import io.techery.snapper.dataset.DataSetChange;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.ItemRef;

public class DataView<T> extends DataSet<T> implements IDataView<T>, IDataSet.Listener<T> {

    protected final List<ItemRef<T>> items = new CopyOnWriteArrayList<>();
    private Predicate<T> predicate;
    private Comparator<T> comparator;

    private List<ItemRef<T>> build(IDataSet<T> dataSet) {

        Queryable<ItemRef<T>> queryable = Queryable.from(dataSet);

        Queryable<ItemRef<T>> where = queryable.where(new Predicate<ItemRef<T>>() {
            @Override
            public boolean apply(ItemRef<T> element) {
                return DataView.this.predicate.apply(element.getValue());
            }
        });

        Queryable<ItemRef<T>> sort = where.sort(new Comparator<ItemRef<T>>() {
            @Override
            public int compare(ItemRef<T> o1, ItemRef<T> o2) {
                return DataView.this.comparator.compare(o1.getValue(), o2.getValue());
            }
        });

        return sort.toList();
    }

    public DataView(IDataSet<T> dataSet, Predicate<T> predicate, Comparator<T> comparator) {
        this.comparator = comparator;
        this.predicate = predicate;

        this.items.addAll(build(dataSet));

        dataSet.addListener(this);
    }

    public Builder<T> view() {
        return new DataViewBuilder<T>(this).sort(this.comparator);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public T getItem(int index) {
        return this.items.get(index).getValue();
    }

    @Override
    public List<T> toList() {
        return Queryable.from(items).map(new Converter<ItemRef<T>, T>() {
            @Override
            public T convert(ItemRef<T> element) {
                return element.getValue();
            }
        }).toList();
    }

    private int indexForNewItem(ItemRef<T> item) {
        final int index = Collections.binarySearch(this.items, item, new Comparator<ItemRef<T>>() {
            @Override
            public int compare(ItemRef<T> o1, ItemRef<T> o2) {
                return DataView.this.comparator.compare(o1.getValue(), o2.getValue());
            }
        });
        if (index >= 0) {
            return index;
        } else {
            return -index - 1;
        }
    }

    @Override
    public void onDataSetUpdated(IDataSet<T> dataSet, DataSetChange<T> change) {
        didUpdateDataSet(processChange(change));
    }

    private DataSetChange<T> processChange(DataSetChange<T> change) {

        final List<ItemRef<T>> addedItems = processAddedItems(change.getAdded());
        final List<ItemRef<T>> removedItems = processRemovedItems(change.getRemoved());
        final List<ItemRef<T>> updatedItems = processUpdatedItems(change.getUpdated(), removedItems);

        return new DataSetChange<T>(addedItems, updatedItems, removedItems);
    }

    private List<ItemRef<T>> processRemovedItems(List<ItemRef<T>> removed) {
        final List<ItemRef<T>> removedItems = new ArrayList<ItemRef<T>>();

        for (ItemRef<T> item : removed) {
            if (this.items.contains(item)) {
                this.items.remove(item);
                removedItems.add(item);
            }
        }

        return removedItems;
    }

    private List<ItemRef<T>> processUpdatedItems(List<ItemRef<T>> updated, List<ItemRef<T>> localRemovedItems) {
        final List<ItemRef<T>> updatedItems = new ArrayList<ItemRef<T>>();

        for (ItemRef<T> item : updated) {
            if (this.items.contains(item)) {
                if (this.predicate.apply(item.getValue())) {
                    this.items.remove(item);
                    int index = indexForNewItem(item);
                    this.items.add(index, item);
                    updatedItems.add(item);
                } else {
                    this.items.remove(item);
                    localRemovedItems.add(item);
                }
            }
        }

        return updatedItems;
    }

    private List<ItemRef<T>> processAddedItems(List<ItemRef<T>> added) {
        final List<ItemRef<T>> addedItems = new ArrayList<ItemRef<T>>();

        for (ItemRef<T> item : added) {
            if (this.predicate.apply(item.getValue())) {
                int index = indexForNewItem(item);
                this.items.add(index, item);
                addedItems.add(item);
            }
        }

        return addedItems;
    }

    @Override
    public Iterator<ItemRef<T>> iterator() {
        return items.iterator();
    }
}
