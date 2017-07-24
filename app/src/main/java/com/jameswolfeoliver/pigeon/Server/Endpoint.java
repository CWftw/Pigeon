package com.jameswolfeoliver.pigeon.Server;

import android.util.Log;

import com.jameswolfeoliver.pigeon.Models.ClientResponses.Error;
import com.jameswolfeoliver.pigeon.Server.Sessions.Session;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.HashMap;
import java.util.Map;

public abstract class Endpoint {
    // MIME Types
    protected final static String MIME_JSON = "application/json";
    protected final static String MIME_HTML = "text/html";
    protected final static String MIME_PLAIN = "text/plain";
    protected final static String MIME_JPEG = "image/jpeg";
    protected final static String MIME_PNG = "image/png";
    protected SessionManager sessionManager;

    public Endpoint(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public Response serve(IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet(session);
            case POST:
                return onPost(session);
            default:
                return buildJsonError(Error.Codes.YOUR_FAULT, "Method Not allowed", Status.METHOD_NOT_ALLOWED);
        }
    }

    private boolean isSessionValid(IHTTPSession session) {
        return sessionManager.validateSession(new Session());
    }

    abstract protected Response onGet(IHTTPSession session);
    abstract protected Response onPost(IHTTPSession session);

    // Shared static functions
    protected static Response buildHtmlResponse(String responseBody, Status status) {
        return Response.newFixedLengthResponse(status, MIME_HTML, responseBody);
    }

    protected static Response buildJsonResponse(String responseJson, Status status) {
        return Response.newFixedLengthResponse(status, MIME_JSON, responseJson);
    }

    protected static Response buildJsonError(int errorCode, String error, Status status) {
        return Response.newFixedLengthResponse(status, MIME_JSON,
                PigeonApplication.getGson().toJson(new Error(errorCode, error), Error.class));
    }

    protected static String getPostData(IHTTPSession session) {
        Map<String, String> bodyMap = new HashMap<>();
        try {
            session.parseBody(bodyMap);
        } catch (Exception e) {
            Log.e(Endpoint.class.getSimpleName(), "Failed to parse body", e);
            return null;
        }
        return bodyMap.get("postData");
    }

}
