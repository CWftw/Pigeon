package com.jameswolfeoliver.pigeon.Server;

import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by james on 16/10/16.
 */
public class TextServer extends NanoHTTPD {
    public static final String LOG_TAG = TextServer.class.getSimpleName();
    private final static int PORT = 8080;
    private final static String LOGIN_URI = "/login";
    private String loginResponseBody;
    private String pageNotFoundResponseBody;

    public TextServer() throws IOException {
        super(PORT);
        this.pageNotFoundResponseBody = PigeonApplication.getAppContext().getResources().getString(R.string.page_not_found);
    }

    public void setLoginResponse(String body) {
        this.loginResponseBody = body;
    }

    @Override
    public Response serve(IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onClientGet(session);
            case POST:
                return onClientPost(session);
            default:
                return buildOkHtmlResponse(pageNotFoundResponseBody);
        }
    }

    private Response onClientGet(IHTTPSession getSession) {
        switch (getSession.getUri()) {
            case LOGIN_URI:
                return buildOkHtmlResponse(loginResponseBody);
            default:
                return buildOkHtmlResponse(pageNotFoundResponseBody);
        }
    }

    private Response onClientPost(IHTTPSession postSession) {
        Map<String, String> bodyMap = new HashMap<>();

        try {
            postSession.parseBody(bodyMap);
        } catch (IOException e) {
            e.printStackTrace();
            return buildOkHtmlResponse(pageNotFoundResponseBody);
        } catch (ResponseException e) {
            e.printStackTrace();
            return buildOkHtmlResponse(pageNotFoundResponseBody);
        }

        String loginBody = bodyMap.get("postData");
        Log.d(LOG_TAG, loginBody);
        Log.d(LOG_TAG, postSession.getUri());

        switch (postSession.getUri()) {
            case LOGIN_URI:
                if (isLoginBodyValid(loginBody)) {
                    return buildOkHtmlResponse(buildLoggedInResponse());
                }
            default:
                return buildOkHtmlResponse(pageNotFoundResponseBody);
        }
    }

    private boolean isLoginBodyValid(String postBody) {
        return true;
    }

    private String buildLoggedInResponse() {
        return "LOGIN POST PROCESSED";
    }

    private Response buildOkHtmlResponse(String responseBody) {
        return new NanoHTTPD.Response(Response.Status.OK, NanoHTTPD.MIME_HTML, responseBody);
    }
}