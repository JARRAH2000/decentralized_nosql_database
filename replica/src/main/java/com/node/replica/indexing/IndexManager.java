package com.node.replica.indexing;


import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.node.replica.Database.Removable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Component
public class IndexManager implements Removable {
    private static Hashtable<IndexKey, PropertyIndex> databaseIndex;

    private static IndexManager indexManager = new IndexManager();

    private IndexManager() {
        databaseIndex = new Hashtable<>();
    }

    public static IndexManager getIndexManager() {
        return indexManager;
    }

    @PostConstruct
    public void init() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String folder = "/app/replicas/databases";
            File[] databases = new File(folder).listFiles();
            assert databases != null;
            for (File database : databases) {
                File[] indexes = new File(folder + "/" + database.getName() + "/indexes").listFiles();
                for (File index : indexes) {
                    IndexKey indexKey = new IndexKey(database.getName(), index.getName().substring(0, index.getName().length() - 5));
                    JsonNode indexContent = objectMapper.readTree(new File(index.getPath()));
                    databaseIndex.put(indexKey, new PropertyIndex());
                    Iterator<Map.Entry<String, JsonNode>> fields = indexContent.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String propertyName = entry.getKey();
                        JsonNode propertyValue = entry.getValue();
                        List<Path> values = new ArrayList<>();
                        for (JsonNode value : propertyValue) {
                            values.add(Paths.get(value.asText()));
                        }

                        databaseIndex.get(indexKey).addPaths(propertyName, values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION IS THROWN");
        }
    }

    public void createIndex(String database, String property) throws IOException {
        IndexKey indexKey = new IndexKey(database, property);
        databaseIndex.put(indexKey, new PropertyIndex());
        String folder = "/app/replicas/databases/" + database + "/data";
        File directory = new File(folder);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            assert files != null;//check the meaning
            for (File file : files) {
                JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                if (json.has(property)) {
                    databaseIndex.get(indexKey).addPath(json.get(property).toString(), Paths.get(file.getPath()));
                } else {
                    databaseIndex.get(indexKey).addPath("", Paths.get(file.getPath()));
                }
            }
            writeOnDisk(indexKey);
        }
    }

    public Hashtable<IndexKey, PropertyIndex> getDatabaseIndex() {
        return databaseIndex;
    }

    public void writeOnDisk(IndexKey indexKey) throws IOException {
        JSONObject jsonObject = new JSONObject();
        for (String key : databaseIndex.get(indexKey).getIndexes().keySet()) {
            JSONArray array = new JSONArray();
            for (Path path : databaseIndex.get(indexKey).getPaths(key)) {
                array.put(path);
            }
            jsonObject.put(key.toString(), array);
        }
        String folder = "/app/replicas/databases/" + indexKey.database();
        try (FileWriter fileWriter = new FileWriter(folder + "/indexes/" + indexKey.property() + ".json")) {
            fileWriter.write(jsonObject.toString(4));
        }
    }

    private PropertyIndex getIndex(IndexKey indexKey) {
        if (databaseIndex.containsKey(indexKey)) {
            return databaseIndex.get(indexKey);
        }
        return null;
    }


    public PropertyIndex deleteIndex(String database, String property) throws IOException {
        IndexKey indexKey = new IndexKey(database, property);
        PropertyIndex index = getIndex(indexKey);
        if (databaseIndex.containsKey(indexKey)) {
            databaseIndex.remove(indexKey);
            remove(Paths.get("/app/replicas/databases/" + database + "/indexes", property + ".json"));
        }
        return index;
    }

    public boolean remove(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }
}
