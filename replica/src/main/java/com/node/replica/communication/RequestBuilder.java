package com.node.replica.communication;

public class RequestBuilder extends Request {
    public static class Builder extends Request.Builder<Builder> {
        @Override
        public RequestBuilder build() {
            return new RequestBuilder(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
    private RequestBuilder(Builder builder) {
        super(builder);
    }
}
