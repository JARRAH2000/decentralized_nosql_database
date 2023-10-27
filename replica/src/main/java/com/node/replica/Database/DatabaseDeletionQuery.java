package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.indexing.IndexKey;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DatabaseDeletionQuery extends WriteTemplate implements Removable{
    public DatabaseDeletionQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        return indexManager.getDatabaseIndex().containsKey(new IndexKey(request.getProperty("database"), "_id")) ? Response.True : Response.DatabaseDoesNotExist;
    }
    @Override
    public void execute() throws IOException {
        remove(Paths.get("/app/replicas/databases/", request.getProperty("database")));
        indexManager.deleteIndex(request.getProperty("database"), "_id");
    }

    public boolean remove(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        }
        return false;
    }
}
