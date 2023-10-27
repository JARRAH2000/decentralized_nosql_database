package com.node.replica.controller;

import com.node.replica.Database.*;
import com.node.replica.communication.Request;
import com.node.replica.communication.RequestBuilder;
import com.node.replica.indexing.IndexCreationQuery;
import com.node.replica.indexing.IndexDeletionQuery;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class Broadcast {

    @DeleteMapping("broadcast-database-deletion")
    public void deleteDatabaseTemplate(@RequestHeader("database") String database) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).build();
            DatabaseDeletionQuery deleteDataBase = new DatabaseDeletionQuery(request);
            deleteDataBase.execute();
        } catch (IOException e) {
            System.out.println("broadcast-database-deletion FAILED");
        }
    }

    @PostMapping("broadcast-database-creation")
    public void createDatabaseTemplate(@RequestHeader("database") String database,
                                       @RequestHeader("schema") String schema) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("schema", schema).build();
            DatabaseCreationQuery createDataBase = new DatabaseCreationQuery(request);
            createDataBase.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-database-creation FAILED");
        }
    }

    @PostMapping("broadcast-document-creation")
    public void createDocumentTemplate(@RequestHeader("database") String database,
                                       @RequestHeader("document") String document,
                                       @RequestHeader("documentId") String documentId) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("document", document).addProperty("documentId", documentId).build();
            DocumentCreationQuery createDocument = new DocumentCreationQuery(request);
            createDocument.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-document-creation FAILED");
        }
    }

    @DeleteMapping("broadcast-document-deletion")
    public void deleteDocument(@RequestHeader("database") String database,
                               @RequestHeader("documentId") String documentId) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("documentId", documentId).build();
            DocumentDeletionQuery deleteDocument = new DocumentDeletionQuery(request);
            deleteDocument.execute();
        } catch (IOException e) {
            System.out.println("broadcast-document-deletion FAILED");
        }
    }

    @PostMapping("broadcast-schema-updating")
    public void updateSchemaTemplate(@RequestHeader("database") String database,
                                     @RequestHeader("schema") String schema) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("schema", schema).build();
            SchemaUpdatingQuery updateSchema = new SchemaUpdatingQuery(request);
            updateSchema.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-schema-updating FAILED");
        }
    }

    @PostMapping("broadcast-property-creation")
    public void createPropertyTemplate(@RequestHeader("database") String database,
                                       @RequestHeader("documentId") String documentId,
                                       @RequestHeader("document") String document) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("documentId", documentId).addProperty("document", document).build();
            PropertyCreationQuery propertyCreationQuery = new PropertyCreationQuery(request);
            propertyCreationQuery.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-property-creation FAILED");
        }
    }

    @PostMapping("broadcast-property-updating")
    public void updatePropertyTemplate(@RequestHeader("database") String database,
                                       @RequestHeader("documentId") String documentId,
                                       @RequestHeader("document") String document) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("documentId", documentId).addProperty("document", document).build();
            PropertyUpdatingQuery updateProperty = new PropertyUpdatingQuery(request);
            updateProperty.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-property-updating FAILED");
        }
    }

    @DeleteMapping("broadcast-property-deletion")
    public void deletePropertyTemplate(@RequestHeader("database") String database,
                                       @RequestHeader("documentId") String documentId,
                                       @RequestHeader("document") String document) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("documentId", documentId).addProperty("document", document).build();
            PropertyDeletionQuery deleteProperty = new PropertyDeletionQuery(request);
            deleteProperty.writeOnDisk();
        } catch (IOException e) {
            System.out.println("broadcast-property-deletion FAILED");
        }
    }


    @PostMapping("broadcast-index-creation")
    public void createIndexTemplate(@RequestHeader("database") String database,
                                    @RequestHeader("property") String property) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("property", property).build();
            IndexCreationQuery createIndex = new IndexCreationQuery(request);
            createIndex.execute();
        } catch (IOException e) {
            System.out.println("broadcast-index-creation FAILED");
        }
    }

    @DeleteMapping("broadcast-index-deletion")
    public void deleteIndexTemplate(@RequestHeader("database") String database,
                                    @RequestHeader("property") String property) {
        try {
            Request request = new RequestBuilder.Builder().addProperty("database", database).addProperty("property", property).build();
            IndexDeletionQuery deleteIndex = new IndexDeletionQuery(request);
            deleteIndex.execute();
        } catch (IOException e) {
            System.out.println("broadcast-index-deletion FAILED");
        }
    }

}

