package net.cryptic_game.server.http.endpoints;

import net.cryptic_game.server.http.HttpEndpoint;
import net.cryptic_game.server.microservice.MicroService;
import net.cryptic_game.server.utils.JSONBuilder;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.UUID;

import static net.cryptic_game.server.socket.SocketServerUtils.sendHTTP;
import static net.cryptic_game.server.socket.SocketServerUtils.sendRaw;
import static net.cryptic_game.server.utils.JSONBuilder.simple;

public class PlayerLeaderboardEndpoint extends HttpEndpoint {

    public PlayerLeaderboardEndpoint() {
        super("leaderboard");
    }

    @Override
    public JSONObject handleRequest() {
//        JSONBuilder jsonBuilder = JSONBuilder.anJSON()
//                .add("ms", "server")
//                .add("endpoint", Collections.singletonList("leaderboard"))
//                .add("tag", UUID.randomUUID().toString());
//        sendRaw(MicroService.get("currency").getChannel(), jsonBuilder.build());
//
//        sendHTTP(channel, simple("users", ));

        return simple("work-in-progress", true);
    }
}
