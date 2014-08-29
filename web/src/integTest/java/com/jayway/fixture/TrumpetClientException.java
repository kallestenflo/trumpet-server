package com.jayway.fixture;

import javax.ws.rs.core.Response;

public class TrumpetClientException extends RuntimeException {

    public final Response response;

    public TrumpetClientException(Response response) {
        this.response = response;
    }
}
