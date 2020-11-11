package net.cryptic_game.server.server.http.endpoints;

import net.cryptic_game.server.client.Client;
import net.cryptic_game.server.server.http.HttpEndpoint;
import org.json.simple.JSONObject;

import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class PlayersOnlineEndpoint extends HttpEndpoint {

    public PlayersOnlineEndpoint() {
        super("online");
    }

    @Override
    public JSONObject handleRequest() {
        return simple("online", Client.getOnlineCount());
    }
}
