package com.jameswolfeoliver.pigeon.Server;

import fi.iki.elonen.NanoHTTPD;

public class Endpoint {
    private final static String MIME_JSON = "application/json";
    private final static String MIME_HTML = "text/html";

    // TODO: implement cookies
    // response.addHeader("Set-Cookie", "pigeonId=123456789; Domain=" + TextServer.getServerIp());

    protected static NanoHTTPD.Response buildHtmlResponse(String responseBody, NanoHTTPD.Response.Status status) {
        return TextServer.newFixedLengthResponse(status, MIME_HTML, responseBody);
    }

    protected static NanoHTTPD.Response buildJsonResponse(String responseJson, NanoHTTPD.Response.Status status) {
        return TextServer.newFixedLengthResponse(status, MIME_JSON, responseJson);
    }
}