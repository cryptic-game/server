package net.cryptic_game.server.client;

import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Request {

    private Client client;
    private UUID tag;
    private String microService;
    private JSONObject data;

    public Request(Client client, UUID tag, String microService, JSONObject data) {
        this.client = client;
        this.tag = tag;
        this.microService = microService;
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }

    public String getMicroService() {
        return microService;
    }

    public void send(JSONObject data) {
        client.send(JSONBuilder.anJSON()
                .add("tag", tag.toString())
                .add("data", data).build());
    }


}
