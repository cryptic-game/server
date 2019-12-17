package net.cryptic_game.server.http;

import org.json.simple.JSONObject;

public abstract class HttpEndpoint {

    private final String name;

    public HttpEndpoint(final String name) {
        this.name = name;
    }

    public abstract JSONObject handleRequest() throws Exception;

    public String getName() {
        return this.name;
    }
}
