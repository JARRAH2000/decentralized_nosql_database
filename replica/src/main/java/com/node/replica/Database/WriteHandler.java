package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.communication.RequestBuilder;
import com.node.replica.indexing.IndexCreationQuery;
import com.node.replica.indexing.IndexDeletionQuery;
import com.node.replica.indexing.IndexManager;
import org.json.JSONException;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Configuration
public class WriteHandler {
    private IndexManager indexManager;
    public WriteHandler() {
        indexManager = IndexManager.getIndexManager();
    }
    private static final Semaphore documentCreationSemaphore = new Semaphore(1);
    private final Lock lock = new ReentrantLock();
    private final Condition processingDone = lock.newCondition();
    private boolean isProcessing = false;
    public Response createProperty(String database, String documentId, String version, String property, String value) {
        Response response = Response.True;
        try {
            Request request = new RequestBuilder.Builder().
                    setEndpoint("createProperty").
                    setMethod("POST").
                    setBroadCastEndpoint("broadcast-property-creation").
                    addProperty("database", database).
                    addProperty("documentId", documentId).
                    addProperty("version", version).
                    addProperty("property", property).
                    addProperty("value", value).
                    build();
            WriteTemplate writeTemplate = new PropertyCreationQuery(request);
            response = writeTemplate.perform();
            return response;
        } catch (IOException ioException) {
            response = Response.False;
        } catch (JSONException jsonException) {
            response = Response.False;
        }
        return response;
    }

    public Response deleteProperty(String database, String documentId, String property) {
        Response response = Response.True;
        try {
            Request request = new RequestBuilder.Builder().
                    setMethod("DELETE").
                    setEndpoint("deleteProperty").
                    setBroadCastEndpoint("broadcast-property-deletion").
                    addProperty("database", database).
                    addProperty("documentId", documentId).
                    addProperty("property", property).
                    build();
            WriteTemplate writeTemplate = new PropertyDeletionQuery(request);
            response = writeTemplate.perform();
            System.out.println("in handler:" + response);
            return response;
        } catch (IOException e) {
            System.out.println("delete property failed");
            response = Response.False;
        } catch (JSONException e) {
            System.out.println("delete property failed");
            response = Response.False;
        }
        return response;
    }

    public String updateProperty(String database, String documentId, String property, String current, String value) {
        lock.lock();
        try {
            while (isProcessing) {
                try {
                    processingDone.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Thread interrupted while waiting for processing to complete";
                }
            }
            isProcessing = true;
        } finally {
            lock.unlock();
        }

        try {
            Request request = new RequestBuilder.Builder().
                    setEndpoint("updateProperty").
                    setBroadCastEndpoint("broadcast-property-updating").
                    setMethod("POST").
                    addProperty("database", database).
                    addProperty("documentId", documentId).
                    addProperty("property", property).
                    addProperty("value", value).
                    addProperty("current", current).
                    build();
            WriteTemplate writeTemplate = new PropertyUpdatingQuery(request);
            Response returned = writeTemplate.perform();
            System.out.println(returned);
            return returned.toString();

        } catch (IOException e) {
            System.out.println("update property failed");
            return "False";
        } catch (JSONException e) {
            System.out.println("update property failed");
            return "False";
        }
        finally {
            lock.lock();
            try {
                isProcessing = false;
                processingDone.signal();
            } finally {
                lock.unlock();
            }
        }
    }



    public String createDocument(String database, String document, String current) {//check affinity
        Response response = Response.True;
        do {
            try {
                Request request = new RequestBuilder.Builder().
                        setEndpoint("updateSchema").
                        setBroadCastEndpoint("broadcast-document-creation").
                        setMethod("POST").addProperty("database", database).
                        addProperty("document", document).
                        addProperty("current", current).
                        build();
                WriteTemplate writeTemplate = new DocumentCreationQuery(request);
                response = writeTemplate.perform();
                //return response;
            } catch (IOException e) {
                System.out.println("create document failed");
                response = Response.False;
            } catch (JSONException e) {
                System.out.println("create document failed");
                response = Response.False;
            }
        } while (response == Response.False);
        return response.toString();
    }

    public Response updateSchema(String database, String current) {
        if (documentCreationSemaphore.tryAcquire()) {
            try {
                Request request = new RequestBuilder.Builder().
                        setEndpoint("updateSchema").
                        setBroadCastEndpoint("broadcast-schema-updating").
                        setMethod("POST").
                        addProperty("database", database).
                        addProperty("current", current).
                        build();
                WriteTemplate writeTemplate = new SchemaUpdatingQuery(request);
                writeTemplate.perform();
                documentCreationSemaphore.release();
                return Response.True;
            } catch (IOException e) {
                System.out.println("updating schema failed");
            } catch (JSONException e) {
                System.out.println("updating schema failed");
            }
        }
        return Response.False;
    }

    public String deleteDocument(String database, String documentId) {//check affinity
        try {
            Request request = new RequestBuilder.Builder().
                    setMethod("DELETE").
                    setEndpoint("deleteDocument").
                    setBroadCastEndpoint("broadcast-document-deletion").
                    addProperty("database", database).
                    addProperty("documentId", documentId).
                    build();
            WriteTemplate writeTemplate = new DocumentDeletionQuery(request);
            return writeTemplate.perform().toString();
            //return true;
        } catch (IOException e) {
            System.out.println("deleting document failed");
        } catch (JSONException e) {
            System.out.println("deleting document failed");
        }
        return Response.Exception.toString();
        //return false;
    }

    public String createDatabase(String userId, String database, String schema) {
        try {
            Request request = new RequestBuilder.Builder().
                    setEndpoint("createDatabase").
                    setBroadCastEndpoint("broadcast-database-creation").
                    setMethod("POST").
                    addProperty("userId", userId).
                    addProperty("database", database).
                    addProperty("schema", schema).
                    build();
            WriteTemplate writeTemplate = new DatabaseCreationQuery(request);
            return writeTemplate.perform().toString();

        } catch (IOException e) {
            System.out.println("create database failed");
        } catch (JSONException e) {
            System.out.println("create database failed");
        }
        return Response.Exception.toString();
    }

    public String deleteDatabase(String database) {
        try {
            Request request = new RequestBuilder.Builder().
                    setEndpoint("deleteDatabase").
                    setMethod("DELETE").
                    setBroadCastEndpoint("broadcast-database-deletion").
                    addProperty("database", database).
                    build();
            WriteTemplate writeTemplate = new DatabaseDeletionQuery(request);
            return writeTemplate.perform().toString();

        } catch (IOException e) {
            System.out.println("delete database failed");
        } catch (JSONException e) {
            System.out.println("delete database failed");
        }
        return Response.Exception.toString();
    }

    public synchronized String createIndex(String database, String property) {//check affinity
        try {
            Request request = new RequestBuilder.Builder().
                    setEndpoint("createIndex").
                    setBroadCastEndpoint("broadcast-index-creation").
                    setMethod("POST").addProperty("database", database).
                    addProperty("property", property).
                    build();
            System.out.println("IN CREATE IN HANDLER");
            WriteTemplate writeTemplate = new IndexCreationQuery(request);
            return writeTemplate.perform().toString();

        } catch (IOException e) {
            System.out.println("create index failed");
        } catch (JSONException e) {
            System.out.println("create index failed");
        }
        return Response.Exception.toString();
    }

    public String deleteIndex(String database, String property) {//check affinity
        try {
            Request request = new RequestBuilder.Builder().
                    setMethod("DELETE").
                    setEndpoint("deleteIndex").
                    setBroadCastEndpoint("broadcast-index-deletion").
                    addProperty("database", database).
                    addProperty("property", property).
                    build();
            WriteTemplate writeTemplate = new IndexDeletionQuery(request);
            return writeTemplate.perform().toString();

        } catch (IOException e) {
            System.out.println("deleting index failed");
        } catch (JSONException e) {
            System.out.println("deleting index failed");
        }
        return Response.Exception.toString();
    }


}
