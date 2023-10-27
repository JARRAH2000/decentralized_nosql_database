package com.node.replica.Database;

import com.node.replica.communication.Request;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SchemaUpdatingQuery extends WriteTemplate {
    public SchemaUpdatingQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        return Response.True;
    }
    @Override
    public synchronized Response isUpToDate() throws IOException {
        Path path = Paths.get("/app/replicas/databases/" + request.getProperty("database"), "schema.json");
        JSONObject schema = new JSONObject(new String(Files.readAllBytes(path)));
        return schema.getInt("_id") == Integer.parseInt(request.getProperty("current")) ? Response.True : Response.False;
    }

    @Override
    public synchronized void execute() throws IOException {
        Path path = Paths.get("/app/replicas/databases/" + request.getProperty("database"), "schema.json");
        JSONObject schema = new JSONObject(new String(Files.readAllBytes(path)));
        schema.put("_id", schema.getInt("_id") + 1);
        request.updateProperty("schema", schema.toString());
        writeOnDisk();
    }

    public synchronized void writeOnDisk() throws IOException {
        Path path = Paths.get("/app/replicas/databases/" + request.getProperty("database"), "schema.json");
        JSONObject schema = new JSONObject(request.getProperty("schema"));
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(schema.toString(4));
        }
    }

}
