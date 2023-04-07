package com.aiunion.aidesk.model.net;

public class RequestMapping {
    private NanoHTTPD.Method method;
    private String uri;

    public RequestMapping(NanoHTTPD.Method method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public NanoHTTPD.Method getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}
