package com.hazelcast.kubernetes;

@SuppressWarnings("unused")
public class CommandResponse {

    private final String response;

    public CommandResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
