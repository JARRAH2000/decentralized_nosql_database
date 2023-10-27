package com.node.replica.indexing;

import java.util.Objects;

public class IndexKey {
    private final String database;
    private final String property;


    public IndexKey(String database, String property) {
        this.database = database;
        this.property = property;
    }

    public String database(){
        return database;
    }
    public String property(){
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKey that = (IndexKey) o;
        return Objects.equals(database, that.database) &&
                Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, property);
    }
}
