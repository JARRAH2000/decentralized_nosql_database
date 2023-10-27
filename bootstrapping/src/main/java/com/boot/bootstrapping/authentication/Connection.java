package com.boot.bootstrapping.authentication;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class Connection {
    @Bean
    public Connection getConnection() {
        return new Connection();
    }

    public String connect(String userId, String password) {
        try {
            Path path = Paths.get("/app/data/" + userId + ".json");
            JSONObject user = new JSONObject(new String(Files.readAllBytes(path)));
            if (BCrypt.checkpw(password, user.getString("password"))) {
                return user.getString("node");
            }
        } catch (IOException e) {
            System.out.println("Something wrong!");
        }
        return "UNAUTHORIZED";
    }
}
