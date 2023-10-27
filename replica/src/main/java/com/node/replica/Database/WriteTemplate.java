package com.node.replica.Database;

import com.node.replica.communication.Request;
import com.node.replica.indexing.IndexManager;
import org.json.JSONException;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import java.util.stream.Collectors;

@Configuration
public abstract class WriteTemplate {
    protected IndexManager indexManager;

    protected Request request;

    public WriteTemplate(Request request) {
        this.request = request;
        indexManager = IndexManager.getIndexManager();
    }

    public abstract Response validate();
    public final Response perform() throws IOException, JSONException {
        Response isValid = validate();
        if (isValid == Response.True) {
            if (isAffinityNode() == Response.True) {
                if (isUpToDate() == Response.True) {
                    execute();
                    broadcast();
                    return Response.True;
                } else return Response.False;
            } else return delegateAffinityNode();
        } else return isValid;
    }

    public Response isAffinityNode() throws IOException {
        return Response.True;
    }

    public Response isUpToDate() throws IOException {
        return Response.True;
    }

    public abstract void execute() throws IOException;

    public final void broadcast() throws IOException {
        int firstPort = Integer.parseInt(System.getenv("FIRST_PORT"));//1
        int lastPort = Integer.parseInt(System.getenv("LAST_PORT"));//10
        int myPort = Integer.parseInt(System.getenv("MY_PORT"));
        while (firstPort <= lastPort) {
            if (firstPort == myPort) {
                firstPort++;
                continue;
            }
            URL url = new URL("http://node" + (firstPort % 10 == 0 ? 10 : firstPort % 10) + ":" + firstPort + "/" + request.getBroadcastEndpoint());//set receiver
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//start connection
            httpURLConnection.setRequestMethod(request.getMethod());//because gossip for write queries only
            Hashtable<String, String> properties = request.getProperties();
            for (String property : properties.keySet()) {
                httpURLConnection.setRequestProperty(property, properties.get(property));
            }
            httpURLConnection.getInputStream();//execute
            firstPort++;//move to next node in the cluster
        }
    }

    public Response delegateAffinityNode() throws IOException {
        return Response.True;
    }

    @Override
    public final String toString() {
        return "Endpoint: " + request.getEndpoint() +
                "\nBroadcast: " + request.getBroadcastEndpoint() +
                "\nMethod: " + request.getMethod() +
                "\nProperties: " +
                request.getProperties()
                        .entrySet()
                        .stream()
                        .map(property -> property.getKey() + " : " + property.getKey())
                        .collect(Collectors.joining("\n"));
    }
}

