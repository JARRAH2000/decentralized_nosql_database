package com.boot.bootstrapping.model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private final String id;
    private final String password;
    private final String hashedPassword;
    private final String node;

    public User(String id, String password, String hashedPassword, String node) {
        this.id = id;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.node = node;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }

    public String getNode() {
        return node;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Map<String, String> getCredentials() {
        Map<String, String> response = new HashMap<>();
        response.put("userId", getId());
        response.put("password", getPassword());
        return response;
    }
}
