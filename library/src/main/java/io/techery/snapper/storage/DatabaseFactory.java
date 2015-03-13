package io.techery.snapper.storage;

import java.io.IOException;

public interface DatabaseFactory {
    DatabaseAdapter createDatabase(String name) throws IOException;
}
