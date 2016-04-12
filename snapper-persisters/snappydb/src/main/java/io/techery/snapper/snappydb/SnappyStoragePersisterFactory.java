package io.techery.snapper.snappydb;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.IOException;

import io.techery.snapper.storage.StoragePersister;
import io.techery.snapper.storage.StoragePersisterFactory;

public class SnappyStoragePersisterFactory implements StoragePersisterFactory {
    private final Context context;
    private final String dbName;
    private DB db;

    public SnappyStoragePersisterFactory(Context context, String dbName) {
        this.context = context;
        this.dbName = dbName;
    }

    @Override
    public StoragePersister createPersister(String adapterPrefix) throws IOException {
        if (db == null) {
            synchronized (SnappyStoragePersisterFactory.class) {
                try {
                    if (db == null || !db.isOpen()) {
                        db = DBFactory.open(context, dbName);
                    }
                } catch (SnappydbException e) {
                    throw new IOException(e.getLocalizedMessage());
                }
            }
        }
        return new SnappyStoragePersister(db, adapterPrefix);
    }

}
