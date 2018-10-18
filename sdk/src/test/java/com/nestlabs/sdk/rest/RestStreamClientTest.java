package com.nestlabs.sdk.rest;

import com.nestlabs.sdk.ExceptionHandler;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.rest.parsers.ParserException;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

public class RestStreamClientTest {

    @Test(expected = MissingTokenException.class)
    public void testStartWithNull_shouldThrowMissingTokenException() {
        RestStreamClient client = new RestStreamClient.Builder(new OkHttpClient(), new RestConfig(), new DummyParser()).build();
        client.start(null);
    }

    @Test(expected = MissingTokenException.class)
    public void testStartWithEmpty_shouldThrowMissingTokenException() {
        RestStreamClient client = new RestStreamClient.Builder(new OkHttpClient(), new RestConfig(), new DummyParser()).build();
        client.start("");
    }

    private class DummyExceptionHandler implements ExceptionHandler {
        NestException exception = null;
        @Override
        public void handle(NestException value) {
            exception = value;
        }
    }

    private DummyCall makeDummySuccessCall(final String message) {
        return new DummyCall() {
            @Override
            public Response execute() throws IOException {
                return new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(200)
                        .body(new RealResponseBody(null, makeBufferedSource(message)))
                        .build();
            }
        };
    }

    @Test
    public void testStart_shouldReadSuccessfully() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String message = "{}";
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = makeDummySuccessCall(message);
        DummyParser parser = new DummyParser() {
            @Override
            public void parse(String msg) throws ParserException {
                super.parse(msg);
                latch.countDown();
            }
        };

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), parser).build();
        client.start("access_token");
        latch.await();

        assertNotNull(parser.message);
        assertEquals(parser.message, message);
    }

    @Test
    public void testStart_shouldRaiseErrorEventWithParserException() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = makeDummySuccessCall("{}");
        DummyParser parser = new DummyParser() {
            @Override
            public void parse(String msg) throws ParserException {
                throw new ParserException("");
            }
        };

        DummyExceptionHandler handler = new DummyExceptionHandler() {
            @Override
            public void handle(NestException value) {
                super.handle(value);
                latch.countDown();
            }
        };

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), parser)
                .setExceptionHandler(handler)
                .build();
        client.start("access_token");
        latch.await();

        assertNotNull(handler.exception);
        assertTrue(handler.exception instanceof ParserException);
    }

    @Test
    public void testStart_shouldRaiseErrorEventWithNestException_ParsingFailure() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = makeDummySuccessCall("{}");
        DummyParser parser = new DummyParser() {
            @Override
            public void parse(String msg) throws ParserException {
                throw new IndexOutOfBoundsException("");
            }
        };

        DummyExceptionHandler handler = new DummyExceptionHandler() {
            @Override
            public void handle(NestException value) {
                super.handle(value);
                latch.countDown();
            }
        };

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), parser)
                .setExceptionHandler(handler)
                .build();
        client.start("access_token");
        latch.await();

        assertNotNull(handler.exception);
        assertTrue(handler.exception.getCause() instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testStart_shouldRaiseErrorEventWithNestException_NetworkFailure() throws InterruptedException {
        //Throw exception on execute() http request, validate exception is handled and retry attempted
        final CountDownLatch latch = new CountDownLatch(2);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = new DummyCall() {
            @Override
            public Response execute() throws IOException {
                throw new IOException();
            }
        };
        DummyExceptionHandler handler = new DummyExceptionHandler() {
            @Override
            public void handle(NestException value) {
                super.handle(value);
                latch.countDown();
            }
        };
        class DummyBackOff implements BackOff {
            @Override
            public long nextInterval() {
                latch.countDown();
                return 0;
            }

            @Override
            public void reset() { }
        }

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), new DummyParser())
                .setExceptionHandler(handler)
                .setBackOff(new DummyBackOff())
                .build();
        client.start("access_token");
        latch.await();

        assertNotNull(handler.exception);
        assertTrue(handler.exception.getCause() instanceof IOException);
    }

    @Test
    public void testAccumulator_shouldReadUntilMessageEnds() throws InterruptedException {
        //Read the stream and accumulate data until end of message marker "}" or "\n"
        //Then invoke parser.parse to process
        final CountDownLatch latch = new CountDownLatch(1);
        final String message = "{message}";
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = makeDummySuccessCall(message);
        DummyParser parser = new DummyParser() {
            @Override
            public void parse(String msg) throws ParserException {
                super.parse(msg);
                latch.countDown();
            }
        };

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        Whitebox.setInternalState(RestStreamClient.class, "DEFAULT_BYTE_COUNT", 1L);

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), parser).build();
        client.start("access_token");
        latch.await();

        Whitebox.setInternalState(RestStreamClient.class, "DEFAULT_BYTE_COUNT", 2048L);

        assertNotNull(parser.message);
        assertEquals(parser.message, message);
    }

    @Test
    public void testStop_shouldResetBackOffStrategy() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String message = "{message}";
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = makeDummySuccessCall(message);
        class DummyBackOff implements BackOff {
            @Override
            public long nextInterval() {
                return 0;
            }

            @Override
            public void reset() {
                latch.countDown();
            }
        }

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);
        PowerMockito.when(mockedClient.dispatcher())
                .thenReturn(new Dispatcher());

        RestStreamClient client = new RestStreamClient.Builder(mockedClient, new RestConfig(), new DummyParser())
                .setBackOff(new DummyBackOff())
                .build();
        client.start("access_token");
        client.stop();
        latch.await();
    }
}
