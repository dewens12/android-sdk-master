package com.nestlabs.sdk.rest;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;

public class DummyCall implements Call {
    public Boolean visited = false;

    public static Request localHostRequest = new Request.Builder().url("http://localhost").build();
    public static BufferedSource makeBufferedSource(String msg) {
        try {
            InputStream in = IOUtils.toInputStream(msg, "UTF-8");
            return Okio.buffer(Okio.source(in));
        } catch (IOException ignore) { }

        return null;
    }

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Response execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(Callback responseCallback) {
        visited = true;
    }

    @Override
    public void cancel() { }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call clone() {
        return null;
    }
}
