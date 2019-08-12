package net.cryptic_game.server.user;

import net.cryptic_game.server.database.Database;
import org.junit.jupiter.api.Test;

class SessionTest {

    @Test
    void createSession() {
        new Database();
        User user = User.create("testuser", "test@mail.de", "adshgkljahlekjahkjlhewtk345ÖÄD'");
        if(user == null)
            user = User.get("testuser");

        Session session = Session.create(user);
    }
}
