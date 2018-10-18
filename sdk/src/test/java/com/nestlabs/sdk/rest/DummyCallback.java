package com.nestlabs.sdk.rest;

import com.nestlabs.sdk.NestException;

public class DummyCallback implements com.nestlabs.sdk.Callback {
    public Exception exception = null;
    public boolean success = false;

    @Override
    public void onSuccess() {
        success = true;
    }

    @Override
    public void onFailure(NestException exception) {
        this.exception = exception;
    }
}