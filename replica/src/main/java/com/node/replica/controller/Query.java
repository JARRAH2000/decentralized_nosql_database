package com.node.replica.controller;

import com.node.replica.Database.ReadHandler;
import com.node.replica.Database.Response;
import com.node.replica.Database.WriteHandler;
import com.node.replica.authentication.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
public class Query {
    @Autowired
    public WriteHandler writeHandler;
    @Autowired
    public ReadHandler readHandler;

    @GetMapping("connectDatabase")
    public String connectDatabase(@RequestHeader("userId") String userId,
                                  @RequestHeader("password") String password,
                                  @RequestHeader("database") String database) throws IOException {
        if (Authentication.isAuthenticated(userId, password) == Response.Unauthenticated)
            return "UNAUTHORIZED";
        String response = readHandler.connectDatabase(database);
        return response.equals("false") ? "NOT FOUND" : response;
    }

    @GetMapping("readDocumentsWhere")
    public List<String> readDocuments(@RequestHeader("database") String database,
                                      @RequestHeader("property") String property,
                                      @RequestHeader("value") String value) throws IOException {
        return readHandler.readDocumentsWhere(database, property, value);
    }

    @GetMapping("readProperty")
    public String readProperty(@RequestHeader("database") String database,
                               @RequestHeader("documentId") String documentId,
                               @RequestHeader("property") String property) throws IOException {
        return readHandler.readProperty(database, documentId, property);
    }

    @DeleteMapping("deleteProperty")
    public String deleteProperty(@RequestHeader("database") String database,
                                 @RequestHeader("documentId") String documentId,
                                 @RequestHeader("property") String property) {
        return writeHandler.deleteProperty(database, documentId, property).toString();
    }

    //valid message
    @PostMapping("updateProperty")
    public String updateProperty(@RequestHeader("database") String database,
                                 @RequestHeader("documentId") String documentId,
                                 @RequestHeader("property") String property,
                                 @RequestHeader("current") String current,
                                 @RequestHeader("value") String value) {
        return writeHandler.updateProperty(database, documentId, property, current, value).toString();
    }

    @PostMapping("createProperty")
    public String createProperty(@RequestHeader("database")String database,
                                 @RequestHeader("documentId")String documentId,
                                 @RequestHeader("version")String version,
                                 @RequestHeader("property")String property,
                                 @RequestHeader("value")String value) {
        return writeHandler.createProperty(database, documentId, version, property, value).toString();
    }

    //document
    @PostMapping("createDocument")
    public String createDocument(@RequestHeader("database") String database,
                                 @RequestHeader("document") String document,
                                 @RequestHeader("current") String current) {
        return writeHandler.createDocument(database, document, current);
    }

    @GetMapping("readDocument")
    public String readDocument(@RequestHeader("database") String database,
                               @RequestHeader("documentId") String documentId) throws IOException {
        return readHandler.readDocument(database, documentId);
    }


    @DeleteMapping("deleteDocument")
    public String deleteDocument(@RequestHeader("database") String database,
                                 @RequestHeader("documentId") String documentId) {
        return writeHandler.deleteDocument(database, documentId);
    }

    @GetMapping("readAllDocuments")
    public List<String> readAllDocuments(@RequestHeader("database") String database) throws IOException {
        return readHandler.readAllDocuments(database);
    }

    //database
    @PostMapping("createDatabase")
    public String createDatabase(@RequestHeader("userId") String userId,
                                 @RequestHeader("database") String database,
                                 @RequestHeader("schema") String schema) {
        return writeHandler.createDatabase(userId, database, schema);
    }

    @DeleteMapping("deleteDatabase")
    public String deleteDatabase(@RequestHeader("userId") String userId,
                                 @RequestHeader("password") String password,
                                 @RequestHeader("database") String database) {
        if (Authentication.isAuthenticated(userId, password) == Response.Unauthenticated ||
                Authentication.isAdmin(userId, database) == Response.Unauthorized)
            return "UNAUTHORIZED";
        return writeHandler.deleteDatabase(database);
    }

    @PostMapping("updateSchema")
    public String updateSchema(@RequestHeader("database") String database,
                                @RequestHeader("current") String current) {
        return writeHandler.updateSchema(database, current).toString();
    }

    //index
    @PostMapping("createIndex")
    public String createIndex(@RequestHeader("userId") String userId,
                              @RequestHeader("password") String password,
                              @RequestHeader("database") String database,
                              @RequestHeader("property") String property) {
        if (Authentication.isAuthenticated(userId, password) == Response.Unauthenticated ||
                Authentication.isAdmin(userId, database) == Response.Unauthorized)
            return "UNAUTHORIZED";
        return writeHandler.createIndex(database, property);
    }

    @DeleteMapping("deleteIndex")
    public String deleteIndex(@RequestHeader("userId") String userId,
                              @RequestHeader("password") String password,
                              @RequestHeader("database") String database,
                              @RequestHeader("property") String property) {
        if (Authentication.isAuthenticated(userId, password) == Response.Unauthenticated ||
                Authentication.isAdmin(userId, database) == Response.Unauthorized)
            return "UNAUTHORIZED";
        return writeHandler.deleteIndex(database, property);
    }

}
