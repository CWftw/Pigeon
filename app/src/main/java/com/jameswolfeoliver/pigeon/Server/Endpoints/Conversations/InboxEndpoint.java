package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class InboxEndpoint extends Endpoint {

    public static Response serve(IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet(session);
            case POST:
                return onPost(session);
            default:
                return buildHtmlResponse(PigeonServer.getBadRequest(), Status.BAD_REQUEST);
        }
    }

    private static Response onPost(IHTTPSession session) {
        return buildHtmlResponse(PigeonServer.getForbidden(), Status.FORBIDDEN);
    }

    private static Response onGet(IHTTPSession session) {
        return buildHtmlResponse(PigeonServer.getForbidden(), Status.FORBIDDEN);
    }
}
