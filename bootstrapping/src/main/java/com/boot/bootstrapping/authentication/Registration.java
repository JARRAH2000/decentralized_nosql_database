package com.boot.bootstrapping.authentication;

import com.boot.bootstrapping.model.User;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Configuration
public class Registration {

    @Bean
    public Registration getRegistration() {
        return new Registration();
    }

    public synchronized User register() throws IOException {
        Path path = Paths.get("/app/data/users.json");
        JSONObject users = new JSONObject(new String(Files.readAllBytes(path)));
        users.put("userId", users.getInt("userId") + 1);
        String userId = String.valueOf(users.getInt("userId"));
        String password = UUID.randomUUID().toString();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(Integer.parseInt(userId)));
        String node = "http://localhost:" + (8080 + users.getInt("userId") % 10) + "/";
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(users.toString());
        }
        //store the user
        JSONObject user = new JSONObject();
        user.put("userId", userId);
        user.put("password", hashedPassword);
        user.put("node", node);
        try (FileWriter fileWriter = new FileWriter("/app/data/" + userId + ".json")) {
            fileWriter.write(user.toString());
        }
        return new User(userId, password, hashedPassword, node);
    }

    public synchronized void broadcast(User user) throws IOException {
        int firstPort = Integer.parseInt(System.getenv("FIRST_PORT"));//1
        int lastPort = Integer.parseInt(System.getenv("LAST_PORT"));//10
        while (firstPort <= lastPort) {

            URL url = new URL("http://node" + (firstPort % 10 == 0 ? 10 : firstPort % 10) + ":" + firstPort + "/register");//set receiver
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//start connection
            httpURLConnection.setRequestMethod("POST");//because gossip for write queries only
            httpURLConnection.setRequestProperty("userId", user.getId());
            httpURLConnection.setRequestProperty("password", user.getHashedPassword());
            httpURLConnection.setRequestProperty("node", user.getNode());
            httpURLConnection.getInputStream();//execute
            firstPort++;//move to next node in the cluster
        }
    }
}
