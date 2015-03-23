package io.techery.snapper.test.benchmark;

import com.artfulbits.benchmark.Meter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.techery.snapper.BaseTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.model.User;
import io.techery.snapper.storage.StorageChange;

public class SpeedTest extends BaseTestCase {

    DataCollection<User> userStorage;
    static final int BATCH_SIZE = 5000;

    static Meter METER;
    boolean skipOnUpdateMeter;

    @Before
    public void initMeter() {
        METER = Meter.getInstance();
        METER.getConfig().ShowStepCostPercents = false;
        METER.getConfig().ShowTopNLongest = 0;
    }

    @Before
    public void initStorage() {
        userStorage = db.collection(User.class);
        userStorage.addDataListener(new IDataSet.DataListener<User>() {
            @Override public void onDataUpdated(List<User> items, StorageChange<User> change) {
                if (canMeterChange(change)) METER.beat("Storage updated with " + change);
            }
        });
    }

    boolean canMeterChange(StorageChange<User> change) {
        return !(skipOnUpdateMeter || METER == null || !METER.isTracking() || change.isEmpty());
    }

    @After
    public void release() {
        userStorage.clear();
    }

    @Test
    public void batchInsert() {
        METER.start("Batch Insert");
        //
        List<User> users = generateUsers(BATCH_SIZE);
        METER.skip("Generating users");
        //
        userStorage.insertAll(users);
        METER.finish("END of Batch Insert");
    }

    @Test
    public void batchRemove() {
        METER.start("Batch Clear");
        //
        startSkippingUpdateChangeMeter();
        userStorage.insertAll(generateUsers(BATCH_SIZE));
        METER.skip("Inserting users");
        stopSkippingUpdateChangeMeter();
        //
        userStorage.clear();
        METER.finish("END of Batch Clear");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    void startSkippingUpdateChangeMeter() {
        skipOnUpdateMeter = true;
    }

    void stopSkippingUpdateChangeMeter() {
        skipOnUpdateMeter = false;
    }

    List<User> generateUsers(int batchSize) {
        List<User> users = new ArrayList<User>();
        Random randomSource = new Random(batchSize);
        for (int i = 0; i < BATCH_SIZE; i++) {
            int random = randomSource.nextInt();
            users.add(new User(String.valueOf(i), random / 2));
        }
        return users;
    }

}
