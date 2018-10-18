package com.nestlabs.sdk.rest.parsers;

import com.nestlabs.sdk.models.GlobalUpdate;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ObjectModelMapperTest {

    private static final String TEST_GLOBAL_UPDATE_JSON = "/test-global-update.json";

    private class DummyEventHandler implements StreamingEventHandler {

        ErrorMessage error = null;
        GlobalUpdate updateEvent = null;
        Boolean authRevokedEvent = false;

        @Override
        public void handleData(GlobalUpdate eventData) {
            updateEvent = eventData;
        }

        @Override
        public void handleError(ErrorMessage errorMessage) {
            error = errorMessage;
        }

        @Override
        public void handleAuthRevoked() {
            authRevokedEvent = true;
        }
    }

    @Test
    public void testMap_shouldSkipUnknownEvent() throws ParserException {
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        mapper.map(new StreamingEvent("unknown", ""));
        assertNull(handler.error);
        assertNull(handler.updateEvent);
        assertFalse(handler.authRevokedEvent);
    }

    @Test
    public void testMap_shouldTriggerAuthRevokedEvent() throws ParserException {
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        mapper.map(new StreamingEvent("auth_revoked", ""));
        assertNull(handler.error);
        assertNull(handler.updateEvent);
        assertTrue(handler.authRevokedEvent);
    }

    @Test
    public void testMap_shouldParseErrorEvent() throws ParserException {
        //Should also ignore unknown fields
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        String json = "{\"error\":\"unauthorized\",\"type\":\"auth-error\",\"message\":\"unauthorized\",\"instance\":\"2372e4af-c774-495f-b485-5e6a81aa27fe\",\"unknown\":\"ignore\"}";
        mapper.map(new StreamingEvent("error", json));
        assertNotNull(handler.error);
        assertEquals(handler.error.getError(), "unauthorized");
        assertEquals(handler.error.getType(), "auth-error");
        assertNull(handler.updateEvent);
        assertFalse(handler.authRevokedEvent);
    }

    @Test(expected = ParserException.class)
    public void testMap_shouldFailToParseErrorEvent() throws ParserException {
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        String json = "{\"error\":\"unauthorized\"";
        mapper.map(new StreamingEvent("error", json));
    }

    @Test
    public void testMap_shouldParseStructureEvent() throws ParserException, IOException {
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        String json = IOUtils.toString(this.getClass().getResourceAsStream(TEST_GLOBAL_UPDATE_JSON),
                "utf-8");

        mapper.map(new StreamingEvent("put", json));
        assertNull(handler.error);
        assertFalse(handler.authRevokedEvent);
        GlobalUpdate event = handler.updateEvent;
        assertNotNull(event);
        assertNotNull(event.getDevices());
        assertNotNull(event.getMetadata());
        assertNotNull(event.getStructures());
        assertNotNull(event.getDevices().getCameras());
        assertNotNull(event.getDevices().getSmokeCOAlarms());
        assertNotNull(event.getDevices().getThermostats());
        assertEquals(event.getDevices().getCameras().size(), 1);
        assertEquals(event.getDevices().getSmokeCOAlarms().size(), 1);
        assertEquals(event.getDevices().getThermostats().size(), 1);
    }

    @Test(expected = ParserException.class)
    public void testMap_shouldFaileToParseStructureEvent() throws ParserException, IOException {
        DummyEventHandler handler = new DummyEventHandler();
        ObjectModelMapper mapper = new ObjectModelMapper(handler);

        mapper.map(new StreamingEvent("put", "{\"devices\":\"cameras\":{}"));
    }
}
