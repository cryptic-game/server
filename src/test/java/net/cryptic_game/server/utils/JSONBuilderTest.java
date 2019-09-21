package net.cryptic_game.server.utils;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class JSONBuilderTest {

    /*@Mock
    Map<String, Object> jsonMapMock;

    @InjectMocks
    JSONBuilder jsonBuilder = JSONBuilder.anJSON();

    @Test
    void add() {
        assertEquals(jsonBuilder.add("abc", "def"), jsonBuilder);
        then(jsonMapMock).should(times(1)).put("abc", "def");
    }

    @Test
    void createEmptyJSONAndBuild() {
        JSONBuilder jsonBuilder = JSONBuilder.anJSON();
        assertThat(jsonBuilder, instanceOf(JSONBuilder.class));
        assertEquals(jsonBuilder.build(), new JSONObject());
    }

    @Test
    void simple() {
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("abc", "def");
        JSONObject expectJSONObject = new JSONObject(jsonMap);

        assertEquals(JSONBuilder.simple("abc", "def"), expectJSONObject);
    }

    @Test
    void error() {
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("error", "abcdef");
        JSONObject expectJSONObject = new JSONObject(jsonMap);

        assertEquals(JSONBuilder.error("abcdef"), expectJSONObject);
    }*/

}
