package com.node.replica.indexing;

import com.node.replica.Database.Response;
import com.node.replica.Database.WriteTemplate;
import com.node.replica.communication.Request;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndexCreationQuery extends WriteTemplate {
    public IndexCreationQuery(Request request) {
        super(request);
    }
    @Override
    public Response validate() {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.DatabaseDoesNotExist;
        indexKey = new IndexKey(request.getProperty("database"), request.getProperty("property"));
        if (indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.IndexAlreadyExists;
        return Response.True;
    }
    @Override
    public synchronized void execute() throws IOException {
        indexManager.createIndex(request.getProperty("database"), request.getProperty("property"));
    }
}
