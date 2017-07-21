package com.jameswolfeoliver.pigeon.Server;

import android.util.Log;

import com.jameswolfeoliver.pigeon.Models.ClientResponses.Error;
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
    protected final static String MIME_JPEG = "image/jpeg";
    protected final static String MIME_PNG = "image/png";

    // TODO: implement cookies
    // response.addHeader("Set-Cookie", "pigeonId=123456789; Domain=" + PigeonServer.getServerIp());

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
