package com.nestlabs.sdk.rest;

import com.nestlabs.sdk.rest.parsers.Parser;
import com.nestlabs.sdk.rest.parsers.ParserException;

class DummyParser implements Parser {
    String message = null;

    @Override
    public void parse(String msg) throws ParserException {
        message = msg;
    }
}