package io.techery.snapper.snappydb;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.IOException;

import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.DatabaseFactory;

public class SnappyDBFactory implements DatabaseFactory {
    private final Context context;
    private final String dbName;
    private DB db;

    public SnappyDBFactory(Context context) {
        this(context, "snappydb");
    }

    public SnappyDBFactory(Context context, String dbName) {
        this.context = context;
        this.dbName = dbName;
    }

    @Override
    public DatabaseAdapter createDatabase(String adapterPrefix) throws IOException {
        if (db == null) {
            synchronized (SnappyDBFactory.class) {
                try {
                    if (db == null || !db.isOpen()) {
                        db = DBFactory.open(context, dbName);
                    }
                } catch (SnappydbException e) {
                    throw new IOException(e.getLocalizedMessage());
                }
            }
        }
        return new SnappyDBAdapter(db, adapterPrefix);
    }

}
