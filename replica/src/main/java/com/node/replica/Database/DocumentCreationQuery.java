package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.indexing.IndexKey;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DocumentCreationQuery extends WriteTemplate {

    public DocumentCreationQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        return indexManager.getDatabaseIndex().containsKey(new IndexKey(request.getProperty("database"), "_id")) ? Response.True : Response.DatabaseDoesNotExist;
    }
    @Override
    public Response isUpToDate() throws IOException {
        Path path = Paths.get("/app/replicas/databases/" + request.getProperty("database") + "/schema.json");
        JSONObject schema = new JSONObject(new String(Files.readAllBytes(path)));
        int affinityNode = schema.getInt("affinity");
        StringBuilder response = new StringBuilder();
        URL url = new URL("http://node" + (affinityNode) + ":" + (8080 + affinityNode) + "/" + request.getEndpoint());//set receiver
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//start connection
        httpURLConnection.setRequestMethod(request.getMethod());//because gossip for write queries only
        for (String property : request.getProperties().keySet()) {
            httpURLConnection.setRequestProperty(property, request.getProperty(property));
        }
        httpURLConnection.setRequestProperty("current", String.valueOf(schema.getInt("_id")));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) response.append(line);
        bufferedReader.close();
        System.out.println("Here: " + response);
        return response.toString().equals("True") ? Response.True : Response.False;
    }

    @Override
    public void execute() throws IOException {
        try {
            Path schemaPath = Paths.get("/app/replicas/databases/" + request.getProperty("database") + "/schema.json");
            JSONObject jsonObject = new JSONObject(request.getProperty("document"));
            jsonObject.put("_id", new JSONObject(new String(Files.readAllBytes(schemaPath))).get("_id"));
            jsonObject.put("affinity", jsonObject.getInt("_id") % 10 == 0 ? 10 : jsonObject.getInt("_id") % 10);
            jsonObject.put("version", 0);
            request.updateProperty("document", jsonObject.toString());
            request.updateProperty("documentId", UUID.randomUUID().toString());
            writeOnDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeOnDisk() throws IOException {
        String path = "/app/replicas/databases";
        JSONObject jsonObject = new JSONObject(request.getProperty("document"));
        try (FileWriter fileWriter = new FileWriter(path + "/" + request.getProperty("database") + "/data/" + request.getProperty("documentId") + ".json")) {
            fileWriter.write(jsonObject.toString(4));
        }

        for (String property : jsonObject.keySet()) {
            IndexKey indexKey = new IndexKey(request.getProperty("database"), property);
            if (indexManager.getDatabaseIndex().containsKey(indexKey)) {
                indexManager.createIndex(request.getProperty("database"), property);
            }
        }
    }
}
