package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.indexing.IndexKey;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseCreationQuery extends WriteTemplate {


    public DatabaseCreationQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        if (request.getProperty("database").contains(".") || request.getProperty("database").contains("/"))
            return Response.DataBaseInvalidName;
        return !indexManager.getDatabaseIndex().containsKey(new IndexKey(request.getProperty("database"), "_id")) ? Response.True : Response.NameIsUsed;
    }
    @Override
    public void execute() throws IOException {
        JSONObject jsonObject = new JSONObject(request.getProperty("schema"));
        jsonObject.put("affinity", (int) (Math.random() * 10) + 1);
        jsonObject.put("_id", 0);//not in broadcast
        jsonObject.put("version", 0);
        jsonObject.put("adminId", request.getProperty("userId"));
        request.updateProperty("schema", jsonObject.toString());
        writeOnDisk();
    }

    public void writeOnDisk() throws IOException {
        String path = "/app/replicas/databases";
        JSONObject jsonObject = new JSONObject(request.getProperty("schema"));
        Files.createDirectory(Paths.get(path, request.getProperty("database")));

        Files.createDirectory(Paths.get(path + "/" + request.getProperty("database"), "indexes"));
        Files.createDirectory(Paths.get(path + "/" + request.getProperty("database"), "data"));
        Files.createFile(Paths.get(path + "/" + request.getProperty("database"), "schema.json"));
        try (FileWriter fileWriter = new FileWriter(path + "/" + request.getProperty("database") + "/schema.json")) {
            fileWriter.write(jsonObject.toString(4));
        }
        indexManager.createIndex(request.getProperty("database"), "_id");
    }
}
