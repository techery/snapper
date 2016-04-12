package io.techery.snapper.droidsnapper;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.techery.snapper.Snapper;
import io.techery.snapper.converter.ObjectConverterFactory;
import io.techery.snapper.datacollection.DataCollectionFactory;
import io.techery.snapper.datacollection.DefaultDataCollectionFactory;
import io.techery.snapper.datacollection.naming.DefaultDataCollectionNamingFactory;
import io.techery.snapper.datacollection.naming.DataCollectionNamingFactory;
import io.techery.snapper.kryo.KryoConverterFactory;
import io.techery.snapper.snappydb.SnappyStorageFactory;
import io.techery.snapper.snappydb.SnappyStoragePersisterFactory;
import io.techery.snapper.snappydb.SnappySweeper;
import io.techery.snapper.storage.StoragePersisterFactory;
import io.techery.snapper.executor.ExecutorFactory;
import io.techery.snapper.executor.SnapperThreadFactory;
import io.techery.snapper.sweeper.Sweeper;

public class DroidSnapper extends Snapper {

    static volatile Snapper sharedSnapper;

    public static Snapper with(Context context) {
        if (sharedSnapper == null) {
            synchronized (DroidSnapper.class) {
                if (sharedSnapper == null) {
                    sharedSnapper = new Builder(context).build();
                }
            }
        }
        return sharedSnapper;
    }

    private DroidSnapper(DataCollectionNamingFactory namingFactory, DataCollectionFactory collectionFactory, Sweeper sweeper) {
        super(namingFactory, collectionFactory, sweeper);
    }

    public static class Builder {

        private final Context context;
        private String storageFileName;
        private DataCollectionNamingFactory namingFactory;
        private ObjectConverterFactory objectConverterFactory;
        private ExecutorFactory storageExecutorFactory;
        private ExecutorFactory collectionExecutorFactory;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder storageFileName(String name) {
            this.storageFileName = name;
            return this;
        }

        public Builder collectionNaming(DataCollectionNamingFactory namingFactory) {
            this.namingFactory = namingFactory;
            return this;
        }

        public Builder converter(ObjectConverterFactory objectConverterFactory) {
            this.objectConverterFactory = objectConverterFactory;
            return this;
        }

        public Builder collectionExecutor(ExecutorFactory executorFactory) {
            collectionExecutorFactory = executorFactory;
            return this;
        }

        public Builder storageExecutor(ExecutorFactory executorFactory) {
            storageExecutorFactory = executorFactory;
            return this;
        }

        public Snapper build() {
            if (TextUtils.isEmpty(storageFileName)) storageFileName = "snappydb";
            if (namingFactory == null) namingFactory = new DefaultDataCollectionNamingFactory();
            if (objectConverterFactory == null) objectConverterFactory = new KryoConverterFactory();
            if (collectionExecutorFactory == null) collectionExecutorFactory = defaultCollectionExecutorFactory;
            if (storageExecutorFactory == null) storageExecutorFactory = defaultStorageExecutorFactory;
            StoragePersisterFactory persisterFactory = new SnappyStoragePersisterFactory(context, storageFileName);
            SnappyStorageFactory storageFactory = new SnappyStorageFactory(persisterFactory, objectConverterFactory, storageExecutorFactory);
            DefaultDataCollectionFactory collectionFactory = new DefaultDataCollectionFactory(storageFactory, collectionExecutorFactory);
            Sweeper sweeper = new SnappySweeper(context, storageFileName);
            //
            return new DroidSnapper(namingFactory, collectionFactory, sweeper);
        }

        static ExecutorFactory defaultStorageExecutorFactory = new ExecutorFactory() {
            ExecutorService shared;

            @Override
            public ExecutorService createExecutor() {
                if (shared == null) shared = Executors.newSingleThreadExecutor(new SnapperThreadFactory("storage"));
                return shared;
            }
        };

        static ExecutorFactory defaultCollectionExecutorFactory = new ExecutorFactory() {
            ExecutorService shared;

            @Override
            public ExecutorService createExecutor() {
                if (shared == null) shared = Executors.newFixedThreadPool(4, new SnapperThreadFactory("collection"));
                return shared;
            }
        };

    }
}
