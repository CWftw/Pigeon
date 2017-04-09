package com.jameswolfeoliver.pigeon.Server.Endpoints.Contacts;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.TextServer;

import fi.iki.elonen.NanoHTTPD;

public class ContactsEndpoint extends Endpoint {

    public static NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet();
            case POST:
                return onPost(session);
            default:
                return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
        }
    }

    private static NanoHTTPD.Response onGet() {
        return buildHtmlResponse(TextServer.getLoginInsecure(), NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onPost(NanoHTTPD.IHTTPSession session) {
        // TODO implement create contact
        return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
    }
}
