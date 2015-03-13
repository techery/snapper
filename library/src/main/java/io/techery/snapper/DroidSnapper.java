package io.techery.snapper;

import android.content.Context;

import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.snappydb.SnappyComponentFactory.DefaultSnappyComponentFactory;
import io.techery.snapper.snappydb.SnappyDBFactory;
import io.techery.snapper.snappydb.SnappyStorageFactory;
import io.techery.snapper.storage.DatabaseFactory;
import io.techery.snapper.storage.StorageFactory;

public class DroidSnapper extends Snapper {

    static volatile Snapper sharedSnapper;

    public static Snapper with(Context context) {
        if (sharedSnapper == null) {
            synchronized (DroidSnapper.class) {
                if (sharedSnapper == null) {
                    sharedSnapper = new SnapperBuilder(context).build();
                }
            }
        }
        return sharedSnapper;
    }

    DroidSnapper(SnappyStorageFactory storageFactory, SnappyComponentFactory componentFactory) {
        super(storageFactory, componentFactory);
    }

    public static class SnapperBuilder {

        private Context context;
        private DatabaseFactory dbFactory;
        private ComponentFactory componentFactory;
        private StorageFactory storageFactory;

        public SnapperBuilder(Context context) {
            this.context = context.getApplicationContext();
            dbFactory = new SnappyDBFactory(context);
        }

        public SnapperBuilder dbFactory(DatabaseFactory dbFactory) {
            this.dbFactory = dbFactory;
            return this;
        }

        public SnapperBuilder componentFactory(ComponentFactory componentFactory) {
            this.componentFactory = componentFactory;
            return this;
        }

        public SnapperBuilder storageFactory(StorageFactory storageFactory) {
            this.storageFactory = storageFactory;
            return this;
        }

        public Snapper build() {
            if (dbFactory == null) useDefaultDatabaseFactory();
            if (componentFactory == null) useDefaultComponentFactory(dbFactory);
            if (storageFactory == null) useDefaultStorageFactory(componentFactory);

            return new Snapper(storageFactory, componentFactory);
        }

        ///////////////////////////////////////////////////////////////////////////
        // Defaults
        ///////////////////////////////////////////////////////////////////////////

        public DatabaseFactory useDefaultDatabaseFactory() {
            dbFactory = new SnappyDBFactory(context);
            return dbFactory;
        }

        public DatabaseFactory useDefaultDatabaseFactory(String dbName) {
            dbFactory = new SnappyDBFactory(context, dbName);
            return dbFactory;
        }

        public ComponentFactory useDefaultComponentFactory(DatabaseFactory dbFactory) {
            componentFactory = new DefaultSnappyComponentFactory(dbFactory);
            return componentFactory;
        }

        public StorageFactory useDefaultStorageFactory(ComponentFactory componentFactory) {
            this.storageFactory = new SnappyStorageFactory(componentFactory);
            return storageFactory;
        }

    }
}
