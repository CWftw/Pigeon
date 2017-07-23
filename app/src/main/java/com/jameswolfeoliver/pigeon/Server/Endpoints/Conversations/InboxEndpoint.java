package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class InboxEndpoint extends Endpoint {

    public InboxEndpoint(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected Response onPost(IHTTPSession session) {
        return buildHtmlResponse(PigeonServer.getForbidden(), Status.FORBIDDEN);
    }

    @Override
    protected Response onGet(IHTTPSession session) {
        return buildHtmlResponse(PigeonServer.getForbidden(), Status.FORBIDDEN);
    }
}
