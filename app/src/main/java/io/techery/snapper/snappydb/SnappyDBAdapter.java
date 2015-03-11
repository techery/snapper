package io.techery.snapper.snappydb;

import android.util.Log;

import com.snappydb.DB;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import io.techery.snapper.storage.DatabaseAdapter;

public class SnappyDBAdapter implements DatabaseAdapter {

    private final DB snappyDB;
    private final String prefix;

    public SnappyDBAdapter(DB db, String prefix) {
        this.snappyDB = db;
        this.prefix = prefix + ":";
    }

    @Override
    public void close() throws IOException {
        throw new IllegalStateException("SnappyDB should be closed by db factory.");
    }

    @Override
    public void put(byte[] key, byte[] value) {
        try {
            snappyDB.put(getFullKey(key), value);
        } catch (SnappydbException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getFullKey(byte[] bytes) throws UnsupportedEncodingException {
        final String key = new String(bytes, "UTF-8");
        return prefix + key;
    }

    private byte[] getOriginalKey(String key) {
        final int keyLength = key.length() - prefix.length();
        byte originalKey[] = new byte[keyLength];

        for (int i = 0; i < keyLength; i++) {
            originalKey[i] = key.getBytes()[prefix.length() + i];
        }

        if (originalKey.length != 4) {
            throw new InvalidParameterException("Invalid key:" + key);
        }

        return originalKey;
    }

    @Override
    public void delete(byte[] bytes) {
        try {
            snappyDB.del(getFullKey(bytes));
        } catch (SnappydbException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enumerate(EnumerationCallback enumerationCallback) {
        KeyIterator keyIterator = null;
        try {
            ArrayList<Object> objects = new ArrayList<>();
            keyIterator = snappyDB.findKeysIterator(this.prefix);
            for (String[] batch : keyIterator.byBatch(1000)) {
                Log.i("LevelDB", "Load Batch:" + batch.length);
                for (String key : batch) {
                    byte[] value = snappyDB.getBytes(key);
                    Object o = enumerationCallback.onRecord(getOriginalKey(key), value);
                    objects.add(o);
                }
            }
            enumerationCallback.onComplete(objects);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } finally {
            if (keyIterator != null) {
                keyIterator.close();
            }
        }
    }
}
