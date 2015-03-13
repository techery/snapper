package io.techery.snapper.snappydb;

import android.util.Base64;
import android.util.Log;

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
        return prefix + Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private byte[] getOriginalKey(String key) {
        final int keyLength = key.length() - prefix.length();
        byte originalKey[] = new byte[keyLength];

        for (int i = 0; i < keyLength; i++) {
            originalKey[i] = key.getBytes()[prefix.length() + i];
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
    public void enumerate(EnumerationCallback enumerationCallback, boolean withValue) {
        KeyIterator keyIterator = null;
        try {
            ArrayList<Object> allRecords = new ArrayList<>();
            keyIterator = snappyDB.findKeysIterator(this.prefix);
            for (String[] batch : keyIterator.byBatch(1000)) {
                ArrayList<Object> batchResults = new ArrayList<>();
                Log.i("LevelDB", "Load Batch:" + batch.length);
                for (String key : batch) {
                    byte[] value;
                    if (withValue) value = snappyDB.getBytes(key);
                    else value = null;
                    Object record = enumerationCallback.onRecord(getOriginalKey(key), value);
                    if (record != null) batchResults.add(record);
                }
                enumerationCallback.onBatchComplete(batchResults);
                allRecords.addAll(batchResults);
                batchResults.clear();
            }
            Log.i("DatabaseAdapter", "Enumerated " + allRecords.size());
            enumerationCallback.onComplete(allRecords);
        } catch (SnappydbException e) {
            e.printStackTrace();
        } finally {
            if (keyIterator != null) {
                keyIterator.close();
            }
        }
    }
}
