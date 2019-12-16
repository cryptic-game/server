package net.cryptic_game.server.http.endpoints;

import net.cryptic_game.server.http.HttpEndpoint;
import net.cryptic_game.server.microservice.MicroService;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static net.cryptic_game.server.utils.JSONBuilder.anJSON;
import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class MicroServiceStatusEndpoint extends HttpEndpoint {

    public MicroServiceStatusEndpoint() {
        super("status");
    }

    @Override
    public JSONObject handleRequest() {
        final List<JSONObject> jsonObjects = new ArrayList<>();
        MicroService.getOnlineMicroServices().forEach(ms -> jsonObjects.add(anJSON()
                .add("name", ms.getName())
                .build()));
        return simple("status", jsonObjects);
    }
}
