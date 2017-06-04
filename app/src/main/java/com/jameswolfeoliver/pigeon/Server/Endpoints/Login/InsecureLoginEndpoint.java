package com.jameswolfeoliver.pigeon.Server.Endpoints.Login;

import android.content.Intent;

import com.jameswolfeoliver.pigeon.Activities.ConnectionActivity;
import com.jameswolfeoliver.pigeon.Managers.NotificationsManager;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class InsecureLoginEndpoint extends Endpoint{
    public static final String LOG_TAG = InsecureLoginEndpoint.class.getSimpleName();

    // Login errors
    private final static int NO_ERROR = 0;
    private final static int DENIED = 1;
    private final static int TERMS_UNCHECKED = 2;
    private final static int NO_USER_RESPONSE = 3;

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

    private static NanoHTTPD.Response onPost(NanoHTTPD.IHTTPSession session) {
        Map<String, String> bodyMap = new HashMap<>();
        try {
            session.parseBody(bodyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bodyMap = session.getParms();
        return buildJsonResponse(buildLogInResponse(bodyMap, session.getRemoteHostName(), session.getRemoteIpAddress()), NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onGet() {
        return buildHtmlResponse(TextServer.getLoginInsecure(), NanoHTTPD.Response.Status.OK);
    }

    private static String buildLogInResponse(Map<String, String> bodyMap, String name, String ip)  {
        int error = NO_ERROR;
        final boolean termsChecked = bodyMap.get("hasAgreedToTerms").equals("true");
        JSONObject responseJson = new JSONObject();

        try {
            responseJson.put("successful", false);
            // todo handle user approval
            if (!termsChecked) {
                error = TERMS_UNCHECKED;
            } else {
                promptUserForClientAccess(name, ip);
            }
            responseJson.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return responseJson.toString();
    }

    private static void promptUserForClientAccess(final String clientName, final String clientIp) {
        if (PigeonApplication.isApplicationVisible()) {
            // Don't recreate if the user is already there
            Intent confirmConnectionIntent = new Intent(PigeonApplication.getAppContext(), ConnectionActivity.class);
            confirmConnectionIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PigeonApplication.getAppContext().startActivity(confirmConnectionIntent);
        } else {
            NotificationsManager.createNotificationForRemoteLogin(clientName, clientIp);
        }
    }
}
