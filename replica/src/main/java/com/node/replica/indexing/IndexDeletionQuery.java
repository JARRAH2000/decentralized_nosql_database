package com.node.replica.indexing;

import com.node.replica.Database.Response;
import com.node.replica.Database.WriteTemplate;
import com.node.replica.communication.Request;

import java.io.IOException;

public class IndexDeletionQuery extends WriteTemplate {
    public IndexDeletionQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.DatabaseDoesNotExist;
        indexKey = new IndexKey(request.getProperty("database"), request.getProperty("property"));
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.IndexDoesNotExist;
        return Response.True;
    }
    @Override
    public synchronized void execute() throws IOException {
        indexManager.deleteIndex(request.getProperty("database"), request.getProperty("property"));
    }
}
