package io.techery.snapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutorService;

import io.techery.snapper.executor.FixedExecutorFactory;
import timber.log.Timber;

import static io.techery.snapper.droidsnapper.DroidSnapper.Builder;

public abstract class BaseTestCase {

    static {
        Timber.plant(new Timber.DebugTree());
    }

    protected Snapper db;

    @Before
    public void prepareDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //
        Builder builder = new Builder(context);
        builder.storageFileName("snappydb_test");
        builder.collectionExecutor(new FixedExecutorFactory(provideCollectionExecutor()));
        builder.storageExecutor(new FixedExecutorFactory(provideStorageExecutor()));
        //
        db = builder.build();
    }

    @After
    public void clearDb() {
        db.clear();
    }

    protected abstract ExecutorService provideStorageExecutor();

    protected abstract ExecutorService provideCollectionExecutor();
}
