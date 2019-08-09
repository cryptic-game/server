package net.cryptic_game.server.client;

import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Request {

    private Client client;
    private UUID tag;
    private String microservice;
    private JSONObject data;

    public Request(Client client, UUID tag, String microservice, JSONObject data) {
        this.client = client;
        this.tag = tag;
        this.microservice = microservice;
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }

    public String getMicroservice() {
        return microservice;
    }

    public void send(JSONObject data) {
        client.send(JSONBuilder.anJSON()
                .add("tag", tag.toString())
                .add("data", data).build());
    }


}
