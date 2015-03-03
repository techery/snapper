package io.techery.snapper.snappydb;

import com.snappydb.DB;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import io.techery.snapper.storage.DatabaseAdapter;

public class SnappyDBAdapter implements DatabaseAdapter {

    private final DB snappyDB;
    private final String prefix;

    public SnappyDBAdapter(DB db, String prefix) {
        this.snappyDB = db;
        this.prefix = prefix;
    }

    @Override
    public void close() throws IOException {
        throw new IllegalStateException("SnappyDB should be closed by db factory.");
    }

    @Override
    public void put(byte[] bytes, byte[] bytes2) {
        try {
            snappyDB.put(getFullKey(bytes), bytes2);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getFullKey(byte[] bytes) throws UnsupportedEncodingException {
        final String key = new String(bytes, "UTF-8");
        return prefix + ":" + key;
    }

    @Override
    public void delete(byte[] bytes) {
        try {
            snappyDB.del(getFullKey(bytes));
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
            keyIterator = snappyDB.findKeysIterator(this.prefix);
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
