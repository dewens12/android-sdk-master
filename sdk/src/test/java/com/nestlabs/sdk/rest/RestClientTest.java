package com.nestlabs.sdk.rest;

import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.ServerException;
import com.nestlabs.sdk.rest.parsers.Parser;
import com.nestlabs.sdk.rest.parsers.ParserException;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

public class RestClientTest {

    @Test(expected = MissingTokenException.class)
    public void testSetTokenNull_expectMissingTokenException() {
        RestClient client = new RestClient(new OkHttpClient(), new RestConfig(), new DummyParser());
        client.setToken(null);
    }

    @Test(expected = MissingTokenException.class)
    public void testSetTokenEmpty_expectMissingTokenException() {
        RestClient client = new RestClient(new OkHttpClient(), new RestConfig(), new DummyParser());
        client.setToken("");
    }

    @Test(expected = MissingTokenException.class)
    public void testWrite_expectMissingTokenException() {
        RestClient client = new RestClient(new OkHttpClient(), new RestConfig(), new DummyParser());
        client.writeDouble("path", "field", 0f, null);
    }

    @Test
    public void testWrite_shouldResetRedirectUrl() {
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyUnknownHostExceptionCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);
                responseCallback.onFailure(this, new UnknownHostException());
            }
        };
        DummyCall dummyIOExceptionCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);
                responseCallback.onFailure(this, new IOException());
            }
        };

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyUnknownHostExceptionCall)
                .thenReturn(dummyIOExceptionCall);

        RestClient client = new RestClient(mockedClient, new RestConfig(), new DummyParser());
        Whitebox.setInternalState(client, "redirectApiUrl", "http://localhost");
        assertEquals(Whitebox.getInternalState(client, "redirectApiUrl"), "http://localhost");

        client.setToken("access_token");
        client.writeBoolean("path", "field", true, null);

        assertTrue(dummyUnknownHostExceptionCall.visited);
        assertTrue(dummyIOExceptionCall.visited);
        assertEquals(Whitebox.getInternalState(client, "redirectApiUrl"), null);
    }

    @Test
    public void testWrite_shouldFailWithServerException() {
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummyCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);
                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(500)
                        .body(new RealResponseBody(null, null))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCallback dummyCallback = new DummyCallback();

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyCall);

        RestClient client = new RestClient(mockedClient, new RestConfig(), new DummyParser());
        client.setToken("access_token");
        client.writeLong("path", "field", 0, dummyCallback);

        assertTrue(dummyCall.visited);
        assertNotNull(dummyCallback.exception);
        assertTrue(dummyCallback.exception instanceof ServerException);
    }

    @Test
    public void testWrite_shouldSwitchToRedirectUrl() {
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        final String redirectUrl = "http://127.0.0.1";
        DummyCall dummyRedirectCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);

                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(307)
                        .addHeader("Location", redirectUrl)
                        .body(new RealResponseBody(null, null))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCall dummySuccessCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);

                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(200)
                        .body(new RealResponseBody(null, makeBufferedSource("{}")))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCallback dummyCallback = new DummyCallback();

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyRedirectCall)
                .thenReturn(dummySuccessCall);

        RestClient client = new RestClient(mockedClient, new RestConfig(), new DummyParser());
        client.setToken("access_token");
        client.writeLong("path", "field", 0, dummyCallback);

        assertTrue(dummyCallback.success);
        assertEquals(Whitebox.getInternalState(client, "redirectApiUrl"), redirectUrl);
    }

    @Test
    public void testWrite_shouldReturnMalformedException() {
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        final String redirectUrl = "invalid_url";
        DummyCall dummyRedirectCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);

                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(307)
                        .addHeader("Location", redirectUrl)
                        .body(new RealResponseBody(null, null))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCallback dummyCallback = new DummyCallback();

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummyRedirectCall);

        RestClient client = new RestClient(mockedClient, new RestConfig(), new DummyParser());
        client.setToken("access_token");
        client.writeLong("path", "field", 0, dummyCallback);

        assertNotNull(dummyCallback.exception);
        assertTrue(dummyCallback.exception instanceof NestException);
        assertNotNull(dummyCallback.exception.getCause());
        assertTrue(dummyCallback.exception.getCause() instanceof MalformedURLException);
    }

    @Test
    public void testWrite_shouldReturnParserException() {
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);

        DummyCall dummySuccessCall = new DummyCall() {
            @Override
            public void enqueue(Callback responseCallback) {
                super.enqueue(responseCallback);

                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(200)
                        .body(new RealResponseBody(null, makeBufferedSource("{}")))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCallback dummyCallback = new DummyCallback();

        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(dummySuccessCall);

        RestClient client = new RestClient(mockedClient, new RestConfig(), new Parser(){
            @Override
            public void parse(String msg) throws ParserException {
                throw new ParserException("");
            }
        });
        client.setToken("access_token");
        client.writeLong("path", "field", 0, dummyCallback);

        assertNotNull(dummyCallback.exception);
        assertTrue(dummyCallback.exception instanceof ParserException);
    }

}
