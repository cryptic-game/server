package net.cryptic_game.server.sql;

import java.io.IOException;

public class SqlException extends IOException {

    SqlException(final String message) {
        super(message);
    }

    SqlException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
