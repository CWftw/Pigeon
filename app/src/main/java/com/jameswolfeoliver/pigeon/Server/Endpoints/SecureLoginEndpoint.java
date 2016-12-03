package com.jameswolfeoliver.pigeon.Server.Endpoints;

import android.os.Build;
import android.util.Log;
import com.jameswolfeoliver.pigeon.Managers.SecurityHelper;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jaredrummler.android.device.DeviceName;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class SecureLoginEndpoint extends Endpoint {
    public static final String LOG_TAG = SecureLoginEndpoint.class.getSimpleName();

    // Login errors
    private final static int NO_ERROR = 0;
    private final static int INVALID_CREDENTIALS = 1;
    private final static int TERMS_UNCHECKED = 2;

    // Login Vars
    private static final String DEVICE_NAME = DeviceName.getDeviceName();

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
        return buildJsonResponse(buildLogInResponse(bodyMap), NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onGet() {
        return buildHtmlResponse(TextServer.getLoginSecure(), NanoHTTPD.Response.Status.OK);
    }

    private static String buildLogInResponse(Map<String, String> bodyMap)  {
        int error = NO_ERROR;
        final String deviceName = bodyMap.get("deviceName").trim();
        final String password = bodyMap.get("password").trim();
        final boolean termsChecked = bodyMap.get("hasAgreedToTerms").equals("true");
        JSONObject responseJson = new JSONObject();

        Log.d(LOG_TAG, String.format("On Thread: %s", Thread.currentThread().getName()));
        Log.d(LOG_TAG, String.format("DEVICE_NAME: %s password: %s checkBox: %b", deviceName, password, termsChecked));

        try {
            responseJson.put("successful", false);
            if (!termsChecked) {
                error = TERMS_UNCHECKED;
            } else if (!deviceName.equals(DEVICE_NAME)) {
                error = INVALID_CREDENTIALS;
            } else if (!SecurityHelper.getInstance().checkUserPassword(password)) {
                error = INVALID_CREDENTIALS;
            } else {
                responseJson.put("successful", true);
            }
            responseJson.put("error", error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, responseJson.toString());
        return responseJson.toString();
    }
}
