package net.cryptic_game.server.user;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void createTest() {
        User user = User.create("testuser", "test@mail.de", "adshgkljahlekjahkjlhewtk345ÖÄD'");

        System.out.println(User.create("testuser", "test@mail.de", "34lkdnat4§a4rg")); //expect null
    }

    @Test
    void createGetTest() {
        User user = User.create("testuser", "test@mail.de", "adshgkljahlekjahkjlhewtk345ÖÄD'");
        if(user == null)
            user = User.get("testuser");

        System.out.println(user.getUUID());

        User user2 = User.get(user.getUUID());
        System.out.println(user2.getName());
    }

    @Test
    void createDeleteTest() {
        User user = User.create("testuser", "test@mail.de", "adshgkljahlekjahkjlhewtk345ÖÄD'");
        if(user == null)
            user = User.get("testuser");

        user.delete();
    }

    @Test
    void createChangePasswordTest() {
        User user = User.create("testuser", "test@mail.de", "adshgkljahlekjahkjlhewtk345ÖÄD'");
        if(user == null)
            user = User.get("testuser");

        System.out.println(user.changePassword("adshgkljahlekjahkjlhewtk345ÖÄD'", "4o6alögj'aflkj_dflakgj"));
    }
}
