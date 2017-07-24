package com.jameswolfeoliver.pigeon.Server.Endpoints.Assets;


import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class JsEndpoint extends Endpoint {
    public JsEndpoint(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected Response onGet(IHTTPSession session) {
        return Response.newFixedLengthResponse(Status.OK, MIME_PLAIN, PigeonServer.getInboxJs());
    }

    @Override
    protected Response onPost(IHTTPSession session) {
        return Response.newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, "", new byte[0]);
    }
}
