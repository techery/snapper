package techery.io.snappytest;

import com.snappydb.DB;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.techery.snapper.storage.DatabaseAdapter;

public class SnappyDBAdapter implements DatabaseAdapter {

    private final DB snappyDB;

    public SnappyDBAdapter(DB db) {
        this.snappyDB = db;
    }

    @Override
    public void close() throws IOException {
        try {
            snappyDB.close();
        } catch (SnappydbException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    @Override
    public void put(byte[] bytes, byte[] bytes2) {
        try {
            snappyDB.put(new String(bytes, "UTF-8"), bytes2);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(byte[] bytes) {
        try {
            snappyDB.del(new String(bytes, "UTF-8"));
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enumerate(EnumerationCallback enumerationCallback) {
        KeyIterator keyIterator = null;
        try {
            keyIterator = snappyDB.allKeysIterator();
            for (String[] batch : keyIterator.byBatch(100)) {
                for (String key : batch) {
                    byte[] value = snappyDB.getBytes(key);
                    enumerationCallback.onRecord(key.getBytes(), value);
                }
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        } finally {
            if (keyIterator != null) {
                keyIterator.close();
            }
        }
    }
}
