package io.techery.snapper;

import android.content.Context;

import io.techery.snapper.snappydb.SnapperStorageFactory;
import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.snappydb.SnappyDBFactory;
import io.techery.snapper.storage.StorageFactory;

public class DroidSnapper extends Snapper {

    private static DroidSnapper sharedSnapper;
    public static SnappyDBFactory dbFactory;

    public static synchronized Snapper get(Context context) {
        if (sharedSnapper == null) {
            dbFactory = new SnappyDBFactory(context);

            SnappyComponentFactory componentFactory = new SnappyComponentFactory(dbFactory);

            SnapperStorageFactory keyValueStorageFactory = new SnapperStorageFactory(componentFactory);

            sharedSnapper = new DroidSnapper(keyValueStorageFactory);
        }

        return sharedSnapper;
    }

    private DroidSnapper(StorageFactory storageFactory) {
        super(storageFactory);
    }
}
