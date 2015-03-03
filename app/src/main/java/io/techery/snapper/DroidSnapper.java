package io.techery.snapper;

import android.content.Context;

import io.techery.snapper.snappydb.SnapperKeyValueStorageFactory;
import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.storage.KeyValueStorageFactory;

public class DroidSnapper extends Snapper {

    private static DroidSnapper sharedSnapper;

    public static synchronized Snapper get(Context context) {
        if (sharedSnapper == null) {
            SnappyComponentFactory componentFactory = new SnappyComponentFactory(context);

            SnapperKeyValueStorageFactory keyValueStorageFactory = new SnapperKeyValueStorageFactory(componentFactory);

            sharedSnapper = new DroidSnapper(keyValueStorageFactory);
        }

        return sharedSnapper;
    }

    private DroidSnapper(KeyValueStorageFactory keyValueStorageFactory) {
        super(keyValueStorageFactory);
    }
}
