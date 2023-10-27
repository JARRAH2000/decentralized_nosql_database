package com.node.replica.Database;

import java.io.IOException;
import java.nio.file.Path;

public interface Removable {
    boolean remove(Path path) throws IOException;
}
