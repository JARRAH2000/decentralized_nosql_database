package com.node.replica.Database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.node.replica.indexing.IndexKey;
import com.node.replica.indexing.IndexManager;
import com.node.replica.indexing.PropertyIndex;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Configuration
public class ReadHandler {
    private IndexManager indexManager;

    public ReadHandler() {
        indexManager = IndexManager.getIndexManager();
    }

    public String readProperty(String database, String documentId, String property) throws IOException {
        String response = "";
        try {
            response = readDocument(database, documentId);
            JSONObject document = new JSONObject(response);
            try {
                response = document.get(property).toString();
            } catch (JSONException propertyException) {
                response = Response.PropertyDoesNotExist.toString();
            }
        } catch (JSONException jsonException) {
            System.out.println(response);
        }
        return response;
    }

    public String connectDatabase(String database) throws IOException {
        Path path = Paths.get("/app/replicas/databases", database + "/schema.json");
        return path.toFile().exists() ? new JSONObject(new String(Files.readAllBytes(path))).toString() : "Database does not exist";
    }

    public List<String> readDocumentsWhere(String database, String property, String value) throws IOException {
        IndexKey indexKey = new IndexKey(database, property);
        List<String> documents = new ArrayList<>();
        if (indexManager.getDatabaseIndex().containsKey(indexKey)) {
            List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(value);
            if (paths != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                for (Path path : paths) {
                    JsonNode document = objectMapper.readTree(new String(Files.readAllBytes(path)));
                    documents.add(document.toString());
                }
            }
        } else {
            Path path = Paths.get("/app/replicas/databases/" + database + "/data");
            File directory = new File(path.toString());
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                assert files != null;
                for (File file : files) {
                    JSONObject document = new JSONObject(new String(Files.readAllBytes(Paths.get(file.getPath()))));
                    if (document.has(property) && document.get(property).toString().equals(value)) {
                        documents.add(document.toString());
                    }
                }
            }
        }
        return documents;
    }

    public List<String> readAllDocuments(String database) throws IOException {
        IndexKey indexKey = new IndexKey(database, "_id");
        List<String> documents = new ArrayList<>();
        if (indexManager.getDatabaseIndex().containsKey(indexKey)) {
            List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getAllPaths();
            for (int i = 0; i < paths.size(); i++) {
                documents.add(new JSONObject(new String(Files.readAllBytes(paths.get(i)))).toString());
            }
        }
        return documents;
    }

    public String readDocument(String database, String documentId) throws IOException { // use index here
        IndexKey indexKey = new IndexKey(database, "_id");
        if (!indexManager.getDatabaseIndex().containsKey(indexKey)) return Response.DatabaseDoesNotExist.toString();
        List<Path> paths = indexManager.getDatabaseIndex().get(indexKey).getPaths(documentId);
        return paths != null ? new JSONObject(new String(Files.readAllBytes(paths.get(0)))).toString() : Response.DocumentDoesNotExist.toString();
    }
}
/*Path path = Paths.get("/app/replicas/databases/" + database + "/data");
        File directory = new File(path.toString());
        List<String> documents = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                documents.add(new JSONObject(new String(Files.readAllBytes(Paths.get(file.getPath())))).toString());
            }
        }*/