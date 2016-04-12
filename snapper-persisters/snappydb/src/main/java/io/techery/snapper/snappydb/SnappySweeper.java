package io.techery.snapper.snappydb;

import android.content.Context;
import android.util.Log;

import com.snappydb.DB;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

import io.techery.snapper.sweeper.Sweeper;

public class SnappySweeper implements Sweeper {

    private final Context context;
    private final String fileName;

    public SnappySweeper(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    @Override
    public void clear() {
        try {
            DB db = new SnappyDB.Builder(context).name(fileName).build();
            db.destroy();
        } catch (SnappydbException e) {
            Log.w("SnappySweeper", "Can't clear db", e);
        }
    }
}
