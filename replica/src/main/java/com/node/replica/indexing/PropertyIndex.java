package com.node.replica.indexing;

import java.nio.file.Path;
import java.util.*;

public class PropertyIndex {
    private Hashtable<String, List<Path>> indexes;

    public PropertyIndex() {
        indexes = new Hashtable<>();
    }

    public List<Path> getPaths(String value) {
        return indexes.get(value);
    }

    public List<Path> getAllPaths() {
        List<Path> paths = new ArrayList<>();
        for (String key : indexes.keySet()) {
            paths.addAll(indexes.get(key));
        }
        return paths;
    }

    public Hashtable<String, List<Path>> getIndexes() {
        return indexes;
    }

    public void addPath(String value, Path path) {
        if (!indexes.containsKey(value)) {
            indexes.put(value, new ArrayList<>());
        }
        indexes.get(value).add(path);
    }

    public void addPaths(String value, List<Path> paths) {
        indexes.put(value, paths);
    }

}
