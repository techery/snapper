package techery.io.snappytest;

import android.content.Context;

import java.io.IOException;

import io.techery.snapper.ComponentFactory;
import io.techery.snapper.storage.DatabaseAdapter;

public class SnappyComponentFactory extends ComponentFactory {

    private final SnappyDBFactory snappyDBFactory;

    public SnappyComponentFactory(Context context) {
        this.snappyDBFactory = new SnappyDBFactory(context);
    }

    @Override
    public DatabaseAdapter createDatabase(String simpleName) throws IOException {
        return this.snappyDBFactory.createDatabase(simpleName);
    }
}
