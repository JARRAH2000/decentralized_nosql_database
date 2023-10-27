package com.boot.bootstrapping.controller;

import com.boot.bootstrapping.authentication.Connection;
import com.boot.bootstrapping.authentication.Registration;
import com.boot.bootstrapping.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


import java.io.*;
import java.util.Map;


@RestController
public class ConnectionController {
    @Autowired
    private Registration registration;
    @Autowired
    private Connection connection;

    @GetMapping("register")
    public ResponseEntity<Map<String, String>> register() throws IOException {
        User user = registration.register();
        registration.broadcast(user);
        return ResponseEntity.ok(user.getCredentials());
    }

    @GetMapping("connect")
    public ResponseEntity<String> connect(@RequestHeader("userId") String userId, @RequestHeader("password") String password) throws IOException {
        String response = connection.connect(userId, password);
        if (!response.equals("UNAUTHORIZED")) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
