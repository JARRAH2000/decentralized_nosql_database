package com.node.replica.authentication;

import com.node.replica.Database.Response;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Authentication {
    public static Response isAuthenticated(String userId, String password) {
        try {
            Path path = Paths.get("/app/replicas/users/" + userId + ".json");
            JSONObject user = new JSONObject(new String(Files.readAllBytes(path)));
            if (BCrypt.checkpw(password, user.getString("password"))) {
                return user.getString("node").equals("http://localhost:" + System.getenv("MY_PORT") + "/")?Response.True:Response.Unauthenticated;
            }
        } catch (IOException e) {
            System.out.println("Authentication");
        }
        return Response.Unauthenticated;
    }

    public static Response isAdmin(String userId, String database) {
        try {
            Path path = Paths.get("/app/replicas/databases/" + database + "/schema.json");
            JSONObject schema = new JSONObject(new String(Files.readAllBytes(path)));

            if (schema.getString("adminId").equals(userId)) {
                return Response.True;
            }
        } catch (IOException e) {
            System.out.println("Authorization");
        }
        return Response.Unauthorized;
    }
}
