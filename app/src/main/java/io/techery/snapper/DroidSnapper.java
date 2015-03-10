package io.techery.snapper;

import android.content.Context;

import io.techery.snapper.snappydb.SnapperStorageFactory;
import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.snappydb.SnappyDBFactory;
import io.techery.snapper.storage.StorageFactory;

public class DroidSnapper extends Snapper {

    static volatile DroidSnapper sharedSnapper;

    public static Snapper with(Context context) {
        if (sharedSnapper == null) {
            synchronized (DroidSnapper.class) {
                if (sharedSnapper == null) {
                    sharedSnapper = createSnapper(context.getApplicationContext());
                }
            }
        }
        return sharedSnapper;
    }

    static DroidSnapper createSnapper(Context context) {
        SnappyDBFactory dbFactory = new SnappyDBFactory(context);
        SnappyComponentFactory componentFactory = new SnappyComponentFactory(dbFactory);
        SnapperStorageFactory keyValueStorageFactory = new SnapperStorageFactory(componentFactory);
        return new DroidSnapper(keyValueStorageFactory);
    }

    DroidSnapper(StorageFactory storageFactory) {
        super(storageFactory);
    }
}
