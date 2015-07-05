package io.techery.snapper.test.benchmark;

import android.support.test.runner.AndroidJUnit4;

import com.artfulbits.benchmark.Meter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.techery.snapper.BaseAsyncTestCase;
import io.techery.snapper.DataCollection;
import io.techery.snapper.dataset.IDataSet;
import io.techery.snapper.dataset.IDataSet.DataListener;
import io.techery.snapper.model.User;
import io.techery.snapper.storage.StorageChange;
import io.techery.snapper.util.ModelUtil;

@RunWith(AndroidJUnit4.class)
public class PerformanceTest extends BaseAsyncTestCase {

    DataCollection<User> userStorage;
    static final int BATCH_SIZE = 10000;

    Meter METER;
    volatile boolean skipDataUpdate;
    MeterDataListener meterDataListener;
    static final String DATA_LISTENER_LOG_TAG = "Storage";

    @Before
    public void initMeter() {
        METER = Meter.getInstance();
        METER.getConfig().ShowStepCostPercents = false;
        METER.getConfig().ShowTopNLongest = 0;
    }

    @Before
    public void initStorage() {
        startSkippingOnChange();
        runWithAwait(1, new Runnable() {
            @Override public void run() {
                userStorage = db.collection(User.class);
                meterDataListener = new MeterDataListener(DATA_LISTENER_LOG_TAG);
                userStorage.addDataListener(meterDataListener);
                userStorage.addDataListener(new InitializeAndResumeListener<User>(userStorage));
            }
        });
    }

    @After
    public void release() {
        startSkippingOnChange();
        if (userStorage.size() > 0 && !userStorage.isClosed()) {
            runWithAwait(1, new Runnable() {
                @Override public void run() {
                    userStorage.removeDataListener(meterDataListener);
                    userStorage.addDataListener(new DataListener<User>() {
                        @Override public void onDataUpdated(List<User> items, StorageChange<User> change) {
                            if (!change.getRemoved().isEmpty()) resume();
                        }
                    });
                    userStorage.clear();
                }
            });
        }
        db.close();
        stopSkippingOnChange();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tests
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void batchInsert() throws Throwable {
        METER.start("Batch Insert");
        //
        stopSkippingOnChange();
        final List<User> users = ModelUtil.generateUsers(BATCH_SIZE);
        METER.skip("Generating users");
        //
        runBatchInsert(users);
        METER.finish("END of Batch Insert");
    }

    protected void runBatchInsert(final List<User> users) {
        runWithAwait(1, new Runnable() {
            @Override public void run() {
                userStorage.insertAll(users);
            }
        });
    }

    @Test
    public void batchRemove() {
        METER.start("Batch Clear");
        //
        startSkippingOnChange();
        runBatchInsert(ModelUtil.generateUsers(BATCH_SIZE));
        //
        stopSkippingOnChange();
        runBatchRemove();
        METER.finish("END of Batch Clear");
    }

    protected void runBatchRemove() {
        runWithAwait(1, new Runnable() {
            @Override public void run() {
                userStorage.clear();
            }
        });
    }

    @Test
    public void load() {
        METER.start("Batch Load");
        //
        startSkippingOnChange();
        runBatchInsert(ModelUtil.generateUsers(BATCH_SIZE));
        db.close();
        userStorage = db.collection(User.class);
        //
        stopSkippingOnChange();
        runBatchLoad();
        METER.finish("END of Batch Load");

    }

    protected void runBatchLoad() {
        runWithAwait(1, new Runnable() {
            @Override public void run() {
                userStorage.addDataListener(meterDataListener);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    class MeterDataListener<T> implements DataListener<T> {

        private final String logTag;

        public MeterDataListener(String logTag) {
            this.logTag = logTag;
        }

        @Override public void onDataUpdated(List<T> items, StorageChange<T> change) {
            if (canReactOnChange(items, change)) {
                if (canMeterChange()) {
                    if (skipDataUpdate) {
                        onSkip(items, change);
                    } else {
                        onBeat(items, change);
                    }
                }
                resume();
            }
        }

        protected void onSkip(List<T> items, StorageChange<T> change) {
            METER.skip(buildMeterLog(items, change));
        }

        protected void onBeat(List<T> items, StorageChange<T> change) {
            METER.beat(buildMeterLog(items, change));
        }

        private String buildMeterLog(List<T> items, StorageChange<T> change) {
            return logTag + " update, items count is " + items.size() + " change is " + change;
        }
    }

    <T> boolean canReactOnChange(List<T> items, StorageChange<T> change) {
        return items.size() == BATCH_SIZE
                || change.getAdded().size() == BATCH_SIZE
                || change.getRemoved().size() == BATCH_SIZE
                || change.getUpdated().size() == BATCH_SIZE;
    }

    boolean canMeterChange() {
        return !(METER == null || !METER.isTracking());
    }

    void startSkippingOnChange() {
        skipDataUpdate = true;
    }

    void stopSkippingOnChange() {
        skipDataUpdate = false;
    }

    class InitializeAndResumeListener<T> implements DataListener<T> {

        IDataSet<T> dataSet;
        boolean isRemoved;

        public InitializeAndResumeListener(IDataSet<T> dataSet) {
            this.dataSet = dataSet;
        }

        @Override public void onDataUpdated(List<T> items, StorageChange<T> change) {
            dataSet.removeDataListener(this);
            if (!isRemoved && items.isEmpty()) {
                isRemoved = true;
                resume();
            }
        }
    }

}
