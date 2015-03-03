package techery.io.snappytest;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.IOException;

import io.techery.snapper.storage.DatabaseAdapter;
import io.techery.snapper.storage.DatabaseFactory;

public class SnappyDBFactory implements DatabaseFactory {
    private final Context context;

    public SnappyDBFactory(Context context) {
        this.context = context;
    }

    @Override
    public DatabaseAdapter createDatabase(String name) throws IOException {
        DB db;
        try {
            db = DBFactory.open(context, name);
        } catch (SnappydbException e) {
            throw new IOException(e.getLocalizedMessage());
        }

        return new SnappyDBAdapter(db);
    }
}
