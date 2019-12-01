package net.cryptic_game.server.error;

import org.json.simple.JSONObject;

import static net.cryptic_game.server.utils.JSONBuilder.error;

public enum ServerError {

    PERMISSION_DENIED("permissions denied"),
    UNSUPPORTED_FORMAT("unsupported format"),
    INVALID_AUTHORIZATION("invalid authorization"),
    MISSING_ACTION("missing action"),
    UNKNOWN_ACTION("unknown action"),
    USERNAME_ALREADY_EXISTS("username already exists"),
    INVALID_EMAIL("invalid email"),
    INVALID_PASSWORD("invalid password"),
    INVALID_TOKEN("invalid token"),
    MISSING_PARAMETERS("missing parameters"),
    UNEXPECTED_ERROR("unexpected error"),
    UNKNOWN_MICROSERVICE("unknown microservice"),
    UNKNOWN_SETTING("unknown setting"),
    UNSUPPORTED_PARAMETER_SIZE("unsupported parameter size");


    private JSONObject response;
    private String message;

    ServerError(String message) {
        this.response = error(message);
        this.message = message;
    }

    public JSONObject getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return message;
    }

}
