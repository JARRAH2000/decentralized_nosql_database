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
import java.util.Hashtable;
import java.util.List;

public class PropertyDeletionQuery extends WriteTemplate {
    public PropertyDeletionQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.DatabaseDoesNotExist;
        List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(request.getProperty("documentId"));
        if (paths == null) return Response.DocumentDoesNotExist;
        try {
            System.out.println("Here in validation");
            JSONObject document = new JSONObject(new String(Files.readAllBytes(paths.get(0))));
            if (!document.has(request.getProperty("property"))) return Response.PropertyDoesNotExist;
            else if (request.getProperty("property").equals("_id") ||
                    request.getProperty("property").equals("affinity") ||
                    request.getProperty("property").equals("version"))
                return Response.MandatoryProperty;
        } catch (IOException ioException) {
            return Response.Exception;
        }
        return Response.True;
    }

    @Override
    public Response isAffinityNode() throws IOException {
        System.out.println("here for affinity");
        List<Path> paths = indexManager.getDatabaseIndex().get(new IndexKey(request.getProperty("database"), "_id")).getPaths(request.getProperty("documentId"));
        if (paths == null || paths.size() == 0) return Response.DocumentDoesNotExist;
        Path path = paths.get(0);
        JSONObject document = new JSONObject(new String(Files.readAllBytes(path)));
        return document.getInt("affinity") == Integer.parseInt(System.getenv("MY_PORT")) % 8080 ? Response.True : Response.False;
    }

    @Override
    public void execute() throws IOException {
        List<Path> paths = indexManager.getDatabaseIndex().get(new IndexKey(request.getProperty("database"), "_id")).getPaths(request.getProperty("documentId"));
        if (paths == null || paths.size() == 0) return;
        Path path = paths.get(0);
        try {
            JSONObject document = new JSONObject(new String(Files.readAllBytes(path)));
            document.remove(request.getProperty("property"));
            document.put("version", document.getInt("version") + 1);
            request.updateProperty("document", document.toString());
            writeOnDisk();
        } catch (IOException e) {
            System.out.println("IO - UpdateID in");
        }
    }

    public void writeOnDisk() throws IOException {
        List<Path> paths = indexManager.getDatabaseIndex().get(new IndexKey(request.getProperty("database"), "_id")).getPaths(request.getProperty("documentId"));
        if (paths == null || paths.size() == 0) return;
        Path path = paths.get(0);
        JSONObject document = new JSONObject(request.getProperty("document"));
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(document.toString(4));
        }
        for (String property : document.keySet()) {
            IndexKey indexKey = new IndexKey(request.getProperty("database"), property);
            if (indexManager.getDatabaseIndex().containsKey(indexKey)) {
                indexManager.createIndex(request.getProperty("database"), property);
            }
        }
    }

    @Override
    public Response delegateAffinityNode() throws IOException {
        System.out.println("here for delegation");

        List<Path> paths = indexManager.getDatabaseIndex().get(new IndexKey(request.getProperty("database"), "_id")).getPaths(request.getProperty("documentId"));
        Path path = paths.get(0);
        JSONObject document = new JSONObject(new String(Files.readAllBytes(path)));
        int affinityNode = document.getInt("affinity");
        StringBuilder response = new StringBuilder();

        URL url = new URL("http://node" + (affinityNode) + ":" + (8080 + affinityNode) + "/" + request.getEndpoint());//set receiver
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//start connection
        httpURLConnection.setRequestMethod(request.getMethod());//because gossip for write queries only
        Hashtable<String, String> properties = request.getProperties();
        for (String property : properties.keySet()) {
            httpURLConnection.setRequestProperty(property, properties.get(property));
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) response.append(line);
        bufferedReader.close();
        System.out.println("Response after delegation: " + response);
        return response.toString().equals("True") ? Response.True : Response.False;
    }
}