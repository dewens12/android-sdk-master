package com.nestlabs.sdk;

import android.content.Intent;

import com.nestlabs.sdk.models.NestToken;
import com.nestlabs.sdk.rest.DummyCall;
import com.nestlabs.sdk.rest.DummyCallback;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

@RunWith(RobolectricTestRunner.class)
public class Oauth2FlowHandlerTest {

    @Test
    public void testGetAccessTokenFromIntent_shouldReturnToken() {
        Intent intent = new Intent();
        NestToken token = new NestToken("token", 123);
        intent.putExtra("access_token_key", token);

        NestToken tokenFromIntent = new Oauth2FlowHandler(new OkHttpClient()).getAccessTokenFromIntent(intent);
        assertNotNull(tokenFromIntent);
        assertEquals(token, tokenFromIntent);
    }

    @Test
    public void testSetGetConfig_shouldWriteReadConfig() {
        Oauth2FlowHandler handler = new Oauth2FlowHandler(new OkHttpClient());

        String clientId = "clientId";
        String clientSecret = "clientSecret";
        String redirectUrl = "redirectUrl";
        handler.setConfig(clientId, clientSecret, redirectUrl);

        assertEquals(handler.getConfig().getClientID(), clientId);
        assertEquals(handler.getConfig().getClientSecret(), clientSecret);
        assertEquals(handler.getConfig().getRedirectURL(), redirectUrl);

        handler.clearConfig();

        assertNull(handler.getConfig());
    }

    @Test
    public void testRevokeToken_shouldRaiseOnFailureEvent_NetworkError() throws Exception {

        DummyCall callInterceptor = new DummyCall() {
            @Override
            public void enqueue(okhttp3.Callback responseCallback) {
                responseCallback.onFailure(this, new IOException(""));
            }
        };

        DummyCallback callback = new DummyCallback();

        NestToken token = new NestToken("test-token", 12345);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);
        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(callInterceptor);

        Oauth2FlowHandler oauth2FlowHandler = new Oauth2FlowHandler(mockedClient);
        oauth2FlowHandler.revokeToken(token, callback);

        assertNotNull(callback.exception);
        assertTrue(callback.exception instanceof NestException);
        assertTrue(callback.exception.getCause() instanceof IOException);
    }

    @Test
    public void testRevokeToken_shouldRaiseOnSuccessEvent() throws Exception {

        DummyCall callInterceptor = new DummyCall() {
            @Override
            public void enqueue(okhttp3.Callback responseCallback) {
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

        DummyCallback callback = new DummyCallback();

        NestToken token = new NestToken("test-token", 12345);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);
        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(callInterceptor);

        Oauth2FlowHandler oauth2FlowHandler = new Oauth2FlowHandler(mockedClient);
        oauth2FlowHandler.revokeToken(token, callback);

        assertTrue(callback.success);
    }


    @Test
    public void testRevokeToken_shouldRaiseOnFailureEvent_ServerError() throws Exception {

        DummyCall callInterceptor = new DummyCall() {
            @Override
            public void enqueue(okhttp3.Callback responseCallback) {
                Response response = new Response.Builder()
                        .request(localHostRequest)
                        .protocol(Protocol.HTTP_1_0)
                        .code(404)
                        .body(new RealResponseBody(null, makeBufferedSource("{}")))
                        .build();

                try {
                    responseCallback.onResponse(this, response);
                } catch (IOException ignore) { }
            }
        };

        DummyCallback callback = new DummyCallback();

        NestToken token = new NestToken("test-token", 12345);
        OkHttpClient mockedClient = PowerMockito.mock(OkHttpClient.class);
        PowerMockito.when(mockedClient.newCall(any(Request.class)))
                .thenReturn(callInterceptor);

        Oauth2FlowHandler oauth2FlowHandler = new Oauth2FlowHandler(mockedClient);
        oauth2FlowHandler.revokeToken(token, callback);

        assertNotNull(callback.exception);
        assertTrue(callback.exception instanceof ServerException);
    }
}
