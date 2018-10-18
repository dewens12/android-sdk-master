package com.nestlabs.sdk.rest.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class MessageParserTest {

    private class DummyMapper implements Mapper {

        StreamingEvent event = null;

        @Override
        public void map(StreamingEvent event) throws ParserException {
            this.event = event;
        }
    }

    @Test
    public void testParse_shouldExitIfEmpty() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        parser.parse(null);
        assertNull(mapper.event);

        parser.parse("");
        assertNull(mapper.event);
    }

    @Test
    public void testParse_shouldSkipMalformedMessages() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        parser.parse("garbage test");
        assertNull(mapper.event);

        parser.parse("event: dummy");
        assertNull(mapper.event);

        parser.parse("event: put");
        assertNull(mapper.event);
    }

    @Test(expected = ParserException.class)
    public void testParse_throwParserExceptionOnInvalidEventFormat() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        parser.parse("event: \ndata: {}");
    }

    @Test(expected = ParserException.class)
    public void testParse_throwParserExceptionOnInvalidDataFormat() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        parser.parse("event: put\ndata: ");
    }

    @Test
    public void testParse_shouldParsePutRevokeEvents() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        String data = "{data}";

        parser.parse("event: put\ndata: " + data);
        assertNotNull(mapper.event);
        assertEquals(mapper.event.getEventType(), "put");
        assertEquals(mapper.event.getMessage(), data);

        parser.parse("event: auth_revoked\ndata: " + data);
        assertNotNull(mapper.event);
        assertEquals(mapper.event.getEventType(), "auth_revoked");
        assertEquals(mapper.event.getMessage(), data);
    }

    @Test
    public void testParse_shouldParseErrorEvent() throws ParserException {
        DummyMapper mapper = new DummyMapper();
        MessageParser parser = new MessageParser(mapper);

        String error = "{\"error\":";

        parser.parse(error);
        assertNotNull(mapper.event);
        assertEquals(mapper.event.getEventType(), "error");
        assertEquals(mapper.event.getMessage(), error);
    }
}
