package io.techery.snapper.snappydb;

import android.util.Base64;

import com.snappydb.DB;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
    public void put(byte[] key, byte[] value) {
        try {
            snappyDB.put(getFullKey(key), value);
        } catch (SnappydbException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getFullKey(byte[] bytes) throws UnsupportedEncodingException {
        return new StringBuilder()
                .append(prefix).append(':')
                .append(Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.NO_PADDING))
                .toString();
    }

    private byte[] getOriginalKey(String key) {
        int prefixLength = prefix.length() + 1;
        final int keyLength = key.length() - prefixLength;
        byte[] keyBytes = key.getBytes();
        byte originalKey[] = new byte[keyLength];

        for (int i = 0; i < keyLength; i++) {
            originalKey[i] = keyBytes[prefixLength + i];
        }

        return Base64.decode(originalKey, Base64.NO_WRAP | Base64.NO_PADDING);
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
    public <T> void enumerate(EnumerationCallback<T> enumerationCallback, boolean withValue) {
        KeyIterator keyIterator = null;
        try {
            ArrayList<T> results = new ArrayList<>();
            for (String key : snappyDB.findKeys(this.prefix)) {
                byte[] value;
                if (withValue) value = snappyDB.getBytes(key);
                else value = null;
                T record = enumerationCallback.onRecord(getOriginalKey(key), value);
                if (record != null) results.add(record);
            }
            enumerationCallback.onComplete(results);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } finally {
            if (keyIterator != null) {
                keyIterator.close();
            }
        }
    }
}
