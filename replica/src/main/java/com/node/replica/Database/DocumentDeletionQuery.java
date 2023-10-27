package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.indexing.IndexKey;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

public class DocumentDeletionQuery extends WriteTemplate implements Removable {
    public DocumentDeletionQuery(Request request) {
        super(request);
    }

    @Override
    public Response validate() {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.DatabaseDoesNotExist;
        List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(request.getProperty("documentId"));
        if (paths == null) return Response.DocumentDoesNotExist;
        return Response.True;
    }

    @Override
    public Response isAffinityNode() throws IOException {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(request.getProperty("documentId"));
        JSONObject document = new JSONObject(new String(Files.readAllBytes(paths.get(0))));
        return document.getInt("affinity") == Integer.parseInt(System.getenv("MY_PORT")) % 8080 ? Response.True : Response.False;
    }

    @Override
    public void execute() throws IOException {
        try {
            IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
            List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(request.getProperty("documentId"));
            JSONObject document = new JSONObject(new String(Files.readAllBytes(paths.get(0))));
            remove(paths.get(0));

            for (String property : document.keySet()) {
                indexKey = new IndexKey(request.getProperty("database"), property);
                if (indexManager.getDatabaseIndex().containsKey(indexKey)) {
                    indexManager.createIndex(request.getProperty("database"), property);
                }
            }
            System.out.println("After update index");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean remove(Path path) throws IOException {
        System.out.println("before delete file");
        return Files.deleteIfExists(path);
    }

    @Override
    public Response delegateAffinityNode() throws IOException {
        IndexKey indexKey = new IndexKey(request.getProperty("database"), "_id");
        List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(request.getProperty("documentId"));
        JSONObject document = new JSONObject(new String(Files.readAllBytes(paths.get(0))));
        int affinityNode = document.getInt("affinity");
        System.out.println("affinity: " + affinityNode);
        StringBuilder response = new StringBuilder();
        URL url = new URL("http://node" + (affinityNode) + ":" + (8080 + affinityNode) + "/" + request.getEndpoint());//set receiver
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//start connection
        httpURLConnection.setRequestMethod(request.getMethod());//because gossip for write queries only
        for (String property : request.getProperties().keySet()) {
            httpURLConnection.setRequestProperty(property, request.getProperty(property));
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) response.append(line);
        bufferedReader.close();
        return response.toString().equals("True") ? Response.True : Response.False;
    }
}
