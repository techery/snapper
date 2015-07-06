package io.techery.snapper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutorService;

import io.techery.snapper.DroidSnapper.SnapperBuilder;
import io.techery.snapper.snappydb.SnappyComponentFactory;
import io.techery.snapper.storage.DatabaseFactory;
import timber.log.Timber;

public abstract class BaseTestCase {

    protected Snapper db;
    protected ExecutorService storageExecutor;
    protected ExecutorService collectionExecutor;

    static {Timber.plant(new Timber.DebugTree());}

    @Before
    public void prepareDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Create Snapper instance with executors on the same thread as tests
        SnapperBuilder snapperBuilder = new SnapperBuilder(context);
        DatabaseFactory databaseFactory = snapperBuilder.useDefaultDatabaseFactory("snappydb_test");
        storageExecutor = provideStorageExecutor();
        collectionExecutor = provideCollectionExecutor();
        db = snapperBuilder.componentFactory(new SnappyComponentFactory(databaseFactory) {

            @Override public ExecutorService createStorageExecutor() {
                return storageExecutor;
            }

            @Override public ExecutorService createCollectionExecutor() {
                return collectionExecutor;
            }
        }).build();
    }

    @After
    public void clearDb() {
        db.clear();
    }

    protected abstract ExecutorService provideStorageExecutor();
    protected abstract ExecutorService provideCollectionExecutor();
}
