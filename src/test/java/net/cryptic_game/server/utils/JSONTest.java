package net.cryptic_game.server.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JSONTest {

    @Mock
    private JSONObject jsonObjectMock;

    @InjectMocks
    private JSON json;

    @Test
    void get_withCast() {
        String key = "abc";
        JSONObject value = new JSONObject();

        given(jsonObjectMock.containsKey(key)).willReturn(true);
        given(jsonObjectMock.get(key)).willReturn(value);

        assertEquals(value, json.get(key, JSONObject.class));
    }

    @Test
    void get_withWrongCast() {
        String key = "abc";
        JSONObject value = new JSONObject();

        given(jsonObjectMock.containsKey(key)).willReturn(true);
        given(jsonObjectMock.get(key)).willReturn(value);

        assertNull(json.get(key, JSONArray.class));
    }

}
