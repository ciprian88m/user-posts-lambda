package dev.ciprian.models.request;

import java.util.Map;

public class GenericRequest {

    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

}
