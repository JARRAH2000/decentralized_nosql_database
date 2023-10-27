package com.node.replica.authentication;

import org.json.JSONObject;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class Registration {

    @PostMapping("register")
    public void register(@RequestHeader("userId") String userId, @RequestHeader("password") String password, @RequestHeader("node") String node) throws IOException {
        Path path = Paths.get("/app/replicas/users");
        if (!path.toFile().exists()) {
            Files.createDirectory(path);
        }
        JSONObject user = new JSONObject();
        user.put("userId", userId);
        user.put("password", password);
        user.put("node", node);
        try (FileWriter fileWriter = new FileWriter(path + "/" + userId + ".json")) {
            fileWriter.write(user.toString());
        }
    }

}
