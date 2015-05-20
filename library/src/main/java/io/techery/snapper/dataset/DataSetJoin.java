package io.techery.snapper.dataset;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.functions.Function2;
import com.innahema.collections.query.functions.Predicate;
import com.innahema.collections.query.queriables.Queryable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.techery.snapper.model.ItemRef;
import io.techery.snapper.storage.StorageChange;

public class DataSetJoin<A, B, OUT> implements IDataSet<OUT> {

    private final IDataSet<A> originalDataSet1;
    private final IDataSet<B> originalDataSet2;
    private final Function2<A, B, Boolean> joinFunction;
    private final Function2<A, List<B>, OUT> mapFunction;
    private final Map<ItemRef<A>, List<B>> cache = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<DataListener<OUT>> listeners = new CopyOnWriteArrayList<DataListener<OUT>>();

    protected DataSetJoin(IDataSet<A> originalDataSet1, IDataSet<B> originalDataSet2, Function2<A, B, Boolean> joinFunction, Function2<A, List<B>, OUT> mapFunction) {
        this.joinFunction = joinFunction;
        this.mapFunction = mapFunction;
        this.originalDataSet1 = originalDataSet1;
        this.originalDataSet2 = originalDataSet2;
        this.originalDataSet1.addDataListener(new LeftJoinListener());
        this.originalDataSet2.addDataListener(new RightJoinListener());
        initialize();
    }

    private void initialize() {
        if (!isInitialized()) return;
        originalDataSet1.perform(new Runnable() {
            @Override public void run() {
                List<OUT> items = new ArrayList<OUT>();
                ArrayList<ItemRef<OUT>> added = new ArrayList<>();
                //
                Iterator<ItemRef<A>> iterator = DataSetJoin.this.originalDataSet1.iterator();
                List<B> joinCandidates = DataSetJoin.this.originalDataSet2.toList();
                while (iterator.hasNext()) {
                    final ItemRef<A> a = iterator.next();
                    List<B> join = Queryable
                            .from(joinCandidates)
                            .where(new Predicate<B>() {
                                @Override public boolean apply(B element) {
                                    return DataSetJoin.this.joinFunction.apply(a.getValue(), element);
                                }
                            }).toList();
                    lock.writeLock().lock();
                    cache.put(a, join);
                    lock.writeLock().unlock();
                    OUT value = convert(a, join);
                    items.add(value);
                    added.add(a.withValue(value));
                }
                //
                StorageChange<OUT> outChange = StorageChange.buildWithAdded(added);
                didUpdate(items, outChange);
            }
        });
    }

    @Override public boolean isInitialized() {
        return originalDataSet1.isInitialized() && originalDataSet1.isInitialized();
    }

    protected void didUpdate(List<OUT> items, StorageChange<OUT> change) {
        for (DataListener<OUT> listener : listeners) {
            listener.onDataUpdated(items, change);
        }
    }

    @Override public void close() {
        throw new UnsupportedOperationException("Transmitter set couldn't be closed directly");
    }

    @Override public boolean isClosed() {
        return originalDataSet1.isClosed() || originalDataSet2.isClosed();
    }

    @Override
    public Iterator<ItemRef<OUT>> iterator() {
        final Iterator<ItemRef<A>> iterator = this.originalDataSet1.iterator();
        return new Iterator<ItemRef<OUT>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ItemRef<OUT> next() {
                ItemRef<A> itemRef = iterator.next();
                OUT value;
                lock.readLock().lock();
                value = convert(itemRef, cache.get(itemRef.getValue()));
                lock.readLock().unlock();
                return itemRef.withValue(value);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public int size() {
        lock.readLock().lock();
        int size = cache.size();
        lock.readLock().unlock();
        return size;
    }

    @Override public List<OUT> toList() {
        List<OUT> list;
        lock.readLock().lock();
            list = Queryable.from(cache.keySet())
                    .map(new Converter<ItemRef<A>, OUT>() {
                        @Override public OUT convert(ItemRef<A> element) {
                            return mapFunction.apply(element.getValue(), cache.get(element));
                        }
                    }).toList();
        lock.readLock().unlock();
        return list;
    }

    @Override
    public void addDataListener(final DataListener<OUT> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeDataListener(DataListener<OUT> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void addStatusListener(StatusListener listener) {
        originalDataSet1.addStatusListener(listener);
        originalDataSet2.addStatusListener(listener);
    }

    @Override
    public void removeStatusListener(StatusListener listener) {
        originalDataSet1.removeStatusListener(listener);
        originalDataSet2.removeStatusListener(listener);
    }

    @Override
    public void perform(Runnable runnable) {
        this.originalDataSet1.perform(runnable);
    }

    private OUT convert(ItemRef<A> item, List<B> join) {
        return convert(item.getValue(), join);
    }

    private OUT convert(A a, List<B> join) {
        return mapFunction.apply(a, join);
    }

    private abstract class JoinListener<T> implements DataListener<T> {
        @Override
        public void onDataUpdated(List<T> items, StorageChange<T> change) {
            StorageChange<OUT> outChange = mapChange(change);
            List<OUT> outs = toList();
            //
            didUpdate(outs, outChange);
        }

        protected abstract StorageChange<OUT> mapChange(StorageChange<T> change);
    }

    private class LeftJoinListener extends JoinListener<A> {
        @Override
        protected StorageChange<OUT> mapChange(StorageChange<A> change) {
            if (change.isEmpty()) return StorageChange.empty();

            List<ItemRef<OUT>> added = new ArrayList<>();
            List<ItemRef<OUT>> updated = new ArrayList<>();
            List<ItemRef<OUT>> removed = new ArrayList<>();
            //
            List<B> joinCandidates = originalDataSet2.toList();
            List<B> join; // reusable ref
            //
            for (ItemRef<A> itemRef : change.getAdded()) {
                join = findJoinCandidates(itemRef.getValue(), joinCandidates);
                lock.writeLock().lock();
                cache.put(itemRef, join);
                lock.writeLock().unlock();
                added.add(itemRef.withValue(convert(itemRef, join)));
            }
            for (ItemRef<A> itemRef : change.getRemoved()) {
                lock.writeLock().lock();
                join = cache.remove(itemRef);
                lock.writeLock().unlock();
                if (join != null) {
                    removed.add(itemRef.withValue(convert(itemRef, join)));
                }
            }
            for (ItemRef<A> itemRef : change.getUpdated()) {
                lock.readLock().lock();
                join = cache.get(itemRef);
                lock.readLock().unlock();
                if (join != null) {
                    lock.writeLock().lock();
                    cache.put(itemRef, join);
                    lock.writeLock().unlock();
                    updated.add(itemRef.withValue(convert(itemRef, join)));
                }
            }
            return new StorageChange<>(added, updated, removed);
        }

        private List<B> findJoinCandidates(final A a, final List<B> itemsRight) {
            return Queryable.from(itemsRight).where(new Predicate<B>() {
                @Override public boolean apply(B element) {
                    return joinFunction.apply(a, element);
                }
            }).toList();
        }

    }

    private class RightJoinListener extends JoinListener<B> {
        @Override
        protected StorageChange<OUT> mapChange(StorageChange<B> change) {
            if (change.isEmpty()) return StorageChange.empty();
            // TODO add sorting for joined values
            lock.readLock().lock();
            List<ItemRef<OUT>> updated = new ArrayList<>();
            for (final ItemRef<A> itemRef : cache.keySet()) {
                boolean wasUpdated = false;
                List<B> join = cache.get(itemRef);
                //
                List<B> newCandidates;
                // process added
                newCandidates = findJoinCandidates(itemRef, change.getAdded());
                if (join.addAll(newCandidates)) {
                    wasUpdated = true;
                }
                // process updated
                newCandidates = findJoinCandidates(itemRef, change.getRemoved());
                if (join.removeAll(newCandidates)) {
                    wasUpdated = true;
                }
                // process removed
                newCandidates = findJoinCandidates(itemRef, change.getUpdated());
                if (join.removeAll(newCandidates)) {
                    join.addAll(newCandidates);
                    wasUpdated = true;
                }
                if (wasUpdated) updated.add(itemRef.withValue(convert(itemRef, join)));
            }
            lock.readLock().unlock();
            return StorageChange.buildWithUpdated(updated);
        }

        private List<B> findJoinCandidates(final ItemRef<A> itemRef, List<ItemRef<B>> updatedJoin) {
            return Queryable.from(updatedJoin)
                    .map(new Converter<ItemRef<B>, B>() {
                        @Override public B convert(ItemRef<B> element) {
                            return element.getValue();
                        }
                    }).where(new Predicate<B>() {
                        @Override public boolean apply(B element) {
                            return joinFunction.apply(itemRef.getValue(), element);
                        }
                    }).toList();
        }
    }

    public static class JoinBuilder<A, B, OUT> {

        private IDataSet<A> originalDataSet1;
        private IDataSet<B> originalDataSet2;
        private Function2<A, B, Boolean> joinFunction;
        private Function2<A, List<B>, ?> mapFunction;

        public JoinBuilder(IDataSet<A> originalDataSet1, IDataSet<B> originalDataSet2) {
            this.originalDataSet1 = originalDataSet1;
            this.originalDataSet2 = originalDataSet2;
        }

        public JoinBuilder<A, B, ?> setJoinFunction(Function2<A, B, Boolean> joinFunction) {
            this.joinFunction = joinFunction;
            return this;
        }

        public <T> JoinBuilder<A, B, T> setMapFunction(Function2<A, List<B>, T> mapFunction) {
            this.mapFunction = mapFunction;
            return (JoinBuilder<A, B, T>) this;
        }

        public DataSetJoin<A, B, OUT> create() {
            if (originalDataSet1 == null) throw new IllegalArgumentException("Left dataset must not be null");
            if (originalDataSet2 == null) throw new IllegalArgumentException("Right dataset must not be null");
            if (joinFunction == null) throw new IllegalArgumentException("Join function must not be null");
            if (mapFunction == null) throw new IllegalArgumentException("Map function must not be null");
            return new DataSetJoin(originalDataSet1, originalDataSet2, joinFunction, mapFunction);
        }
    }
}
