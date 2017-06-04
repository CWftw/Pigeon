package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.TextServer;

import fi.iki.elonen.NanoHTTPD;

public class InboxEndpoint extends Endpoint {

    public static NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet(session);
            case POST:
                return onPost(session);
            default:
                return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
        }
    }

    private static NanoHTTPD.Response onPost(NanoHTTPD.IHTTPSession session) {
        return buildHtmlResponse(TextServer.getForbidden(), NanoHTTPD.Response.Status.FORBIDDEN);
    }

    private static NanoHTTPD.Response onGet(NanoHTTPD.IHTTPSession session) {
        return buildHtmlResponse(TextServer.getForbidden(), NanoHTTPD.Response.Status.FORBIDDEN);
    }
}
