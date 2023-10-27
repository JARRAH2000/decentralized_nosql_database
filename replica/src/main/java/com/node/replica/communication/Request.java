package com.node.replica.communication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

public abstract class Request {
    final Hashtable<String, String> properties;
    private String endpoint;
    private String method;
    private String broadcastEndpoint;

    abstract static class Builder<T extends Builder<T>> {
        Hashtable<String, String> properties = new Hashtable<>();
        private String endpoint;
        private String method;
        private String broadcastEndpoint;
        public T addProperty(String property, String value) {
            properties.put(property, value);
            return self();
        }

        public T setMethod(String method) {
            this.method = method;
            return self();
        }


        public T setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return self();
        }

        public T setBroadCastEndpoint(String broadCastEndpoint) {
            this.broadcastEndpoint = broadCastEndpoint;
            return self();
        }

        abstract Request build();

        protected abstract T self();
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBroadcastEndpoint() {
        return broadcastEndpoint;
    }

    public void updateProperty(String key, String value) {
        properties.put(key, value);
    }

    public Hashtable<String, String> getProperties() {
        return properties;
    }

    Request(Builder<?> builder) {
        properties = new Hashtable<>(builder.properties);
        method = builder.method;
        endpoint = builder.endpoint;
        broadcastEndpoint = builder.broadcastEndpoint;
    }
}
