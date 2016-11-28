package com.jameswolfeoliver.pigeon.Server;

import android.provider.Settings;
import android.util.Log;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by james on 16/10/16.
 */
public class TextServer extends NanoHTTPD {
    public static final String LOG_TAG = TextServer.class.getSimpleName();
    private final static int PORT = 8080;
    private final static String CHARSET_UTF8 = "UTF-8";
    private final static String LOGIN_URI = "/login";
    private final static String MIME_JSON = "application/json";
    private final static int NO_ERROR = 0;
    private final static int INVALID_CREDENTIALS = 1;
    private final static int TERMS_UNCHECKED = 2;
    private String serverIp;
    private String loginResponseBody;
    private String pageNotFoundResponseBody;

    private String deviceName = Settings.System.NAME;

    public TextServer() throws IOException {
        super(PORT);
        System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
        makeSecure(NanoHTTPD.makeSSLSocketFactory("/keystore.jks", "password".toCharArray()), null);
        this.pageNotFoundResponseBody = PigeonApplication.getAppContext().getResources().getString(R.string.page_not_found);
    }

    public void setLoginResponse(String body) {
        this.loginResponseBody = body;
        Log.d(LOG_TAG, "Device Name: " + deviceName);
        this.loginResponseBody = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->\n" +
                "    <meta name=\"Text Messaging, With a Twist\" content=\"\">\n" +
                "    <meta name=\"James\" content=\"\">\n" +
                "    <link rel=\"icon\" href=\"http://www.iconarchive.com/download/i83644/pelfusion/long-shadow-media/Message-Bubble.ico\">\n" +
                "\n" +
                "    <title>Pigeon</title>\n" +
                "\n" +
                "\t<!-- Latest compiled and minified CSS -->\n" +
                "\t<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">\n" +
                "\n" +
                "    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->\n" +
                "    <!--[if lt IE 9]>\n" +
                "      <script src=\"https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js\"></script>\n" +
                "      <script src=\"https://oss.maxcdn.com/respond/1.4.2/respond.min.js\"></script>\n" +
                "    <![endif]-->\n" +
                "  </head>\n" +
                "\n" +
                "  <body >\n" +
                "    \n" +
                "    <div class=\"container\" style=\"position: fixed; top: -50%; left: -50%; width: 200%; height: 200%;\">\n" +
                "      <img src=\"https://d2lm6fxwu08ot6.cloudfront.net/img-thumbs/960w/0XXXZEHMI2.jpg\" alt=\"background\" style=\"position: absolute; top: 0; left: 0; right: 0; bottom: 0; margin: auto; min-width: 50%; min-height: 50%;\">\n" +
                "    </div>\n" +
                "\t\n" +
                "    <div class=\"container col-xs-12\" align=\"center\" style=\"z-index: 10; position:absolute; top: 28vh; opacity: 0.9;\">\n" +
                "\t  <div class=\"row\">\n" +
                "\t\t<div class=\"is-Responsive Absolute-Center\">\n" +
                "\t\t  <div class=\"col-xs-3\"> </div>\n" +
                "\t\t  <div class=\"col-xs-6\" style=\"background: rgba(47, 79, 79, 0.6); border-radius: 25px\">\n" +
                "\t\t\t<h2 align=\"center\" style=\"color: #FFFFFF;\">Pigeon</h2>\n" +
                "\t\t\t<p id=\"loginFeedback\" style=\"color: #FFFFFF;\" align=\"left\"> </p>\n" +
                "\t\t\t<form id=\"loginForm\">\n" +
                "\t\t\t  <div class=\"form-group input-group\">\n" +
                "\t\t\t\t<span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-phone\"> </i></span>\n" +
                "\t\t\t\t<input class=\"form-control\" id=\"deviceNameInput\" type=\"text\" placeholder=\"device name\"/>          \n" +
                "\t\t\t  </div>\n" +
                "\t\t\t  <div class=\"form-group input-group\">\n" +
                "\t\t\t\t<span class=\"input-group-addon\"><i class=\"glyphicon glyphicon-lock\"> </i></span>\n" +
                "\t\t\t\t<input class=\"form-control\" id=\"passwordInput\" type=\"password\" placeholder=\"password\"/>     \n" +
                "\t\t\t  </div>\n" +
                "\t\t\t  <div class=\"checkbox\">\n" +
                "\t\t\t\t<label>\n" +
                "\t\t\t\t  <input id=\"termsCheckbox\" type=\"checkbox\"> <span style=\"color: #FFFFFF;\">I agree to the </span><a style=\"color: #FFFFFF;\" href=\"#\">Terms and Conditions</a>\n" +
                "\t\t\t\t</label>\n" +
                "\t\t\t  </div>\n" +
                "\t\t\t  <div class=\"form-group\">\n" +
                "\t\t\t\t<button type=\"button\" onclick=\"loginPost();\" class=\"btn btn-def btn-block btn-success\">Authenticate</button>\n" +
                "\t\t\t  </div>\n" +
                "\t\t\t  <div class=\"form-group\">\n" +
                "\t\t\t\t<label>\n" +
                "\t\t\t\t  <p style=\"color: #FFFFFF;\">Checkout the <a style=\"color: #FFFFFF;\" href=\"https://github.com/jameswolfeoliver/Pigeon\">Github Repo</a></p>\n" +
                "\t\t\t\t</label>\t\t\t  \n" +
                "\t\t\t  </div>\n" +
                "\t\t\t</form>        \n" +
                "\t\t  </div> \n" +
                "\t\t  <div class=\"col-xs-3\"> </div>\n" +
                "\t\t</div>    \n" +
                "\t  </div>\n" +
                "\t</div>\n" +
                "\n" +
                "   \n" +
                "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js\"></script>\n" +
                "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>\n" +
                "    <script type=\"text/javascript\">\n" +
                "\t\tfunction loginPost() {\n" +
                "\t\t    var URL = window.location.href;\n" +
                "\t\t    console.log(URL);\n" +
                "\t\t    var name = document.getElementById(\"deviceNameInput\").value;\n" +
                "\t\t    console.log(name);\n" +
                "\t\t    var password = document.getElementById(\"passwordInput\").value;\n" +
                "\t\t    console.log(password);\n" +
                "\t\t    var terms = document.getElementById(\"termsCheckbox\").checked;\n" +
                "\t\t    console.log(terms);\n" +
                "\t\t    var data = {};\n" +
                "\t\t    data.deviceName = name;\n" +
                "\t\t    data.password = password;\n" +
                "\t\t    data.hasAgreedToTerms = terms;\n" +
                "\t\t    console.log(data);\n" +
                "\t\t    if(!isEmpty(name.trim()) \n" +
                "\t\t    \t&& !isEmpty(password.trim())){\n" +
                "\t\t    \t\t$.post(URL,data, function( dataRecieved ) {\n" +
                "\t\t\t    \tvar errorFeedback = document.getElementById(\"loginFeedback\");\n" +
                "\t  \t\t\t\tvar error = dataRecieved.error; \n" +
                "\t  \t\t\t\tvar wasSuccessful = dataRecieved.successful;\n" +
                "\t  \t\t\t\tif (!wasSuccessful) {\n" +
                "\t  \t\t\t\t\tswitch (dataRecieved.error) {\n" +
                "\t\t  \t\t\t\t\tcase 0:\n" +
                "\t\t  \t\t\t\t\t\terrorFeedback.innerHTML = \"\";\n" +
                "\t\t  \t\t\t\t\t\t// Do get chat page\n" +
                "\t\t  \t\t\t\t\tcase 1:\n" +
                "\t\t  \t\t\t\t\t\terrorFeedback.innerHTML = \"Please check your device name and/or password\";\n" +
                "\t\t  \t\t\t\t\t\tbreak;\n" +
                "\t\t  \t\t\t\t\tcase 2:\n" +
                "\t\t  \t\t\t\t\t\terrorFeedback.innerHTML = \"Please agree to the terms of service to continue\";\n" +
                "\t\t  \t\t\t\t\t\tbreak;\n" +
                "\t\t  \t\t\t\t\tdefault:\n" +
                "\t\t  \t\t\t\t\t\terrorFeedback.innerHTML = \"Server error, please try again\";\n" +
                "\t  \t\t\t\t\t}\n" +
                "\t  \t\t\t\t} else {\n" +
                "\t  \t\t\t\t\t// do get chat page\n" +
                "\t  \t\t\t\t}\n" +
                "\t  \t\t\t\t\n" +
                "\t\t\t\t}, \"json\");\n" +
                "\t\t    } else {\n" +
                "\t\t\t    var errorFeedback = document.getElementById(\"loginFeedback\");\n" +
                "\t\t\t    errorFeedback.innerHTML = \"Neither device name or password should be empty\";\n" +
                "\t\t    }\n" +
                "\t\t}\n" +
                "\t\tfunction isEmpty(str) {\n" +
                "    \t\treturn (!str || 0 === str.length);\n" +
                "\t\t}\n" +
                "\t</script>\n" +
                "  </body>\n" +
                "</html>\n";
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
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
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        bodyMap = postSession.getParms();

        switch (postSession.getUri()) {
            case LOGIN_URI:
                try {
                    return builOkJsonResponse(buildLogInResponse(bodyMap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            default:
                return buildOkHtmlResponse(pageNotFoundResponseBody);
        }
    }

    private String buildLogInResponse(Map<String, String> bodyMap) throws JSONException {
        int error = NO_ERROR;
        final String deviceName = bodyMap.get("deviceName").trim();
        final String password = bodyMap.get("password").trim();
        final boolean termsChecked = bodyMap.get("hasAgreedToTerms").equals("true");
        JSONObject responseJson = new JSONObject();

        Log.d(LOG_TAG, String.format("deviceName: %s password: %s checkBox: %b", deviceName, password, termsChecked));

        if (!termsChecked) {
            error = TERMS_UNCHECKED;
            responseJson.put("successful", false);
        } else if (!deviceName.equals(this.deviceName)) {
            error = INVALID_CREDENTIALS;
            responseJson.put("successful", false);
        } else {
            Log.d("Can authenticate", Boolean.toString(SecurityHelper.getInstance().checkUserPassword(password, new SecurityHelper.AuthenticationCallback() {
                @Override
                public void onUserAuthenticated() {
                    Log.d("Authenticated", "true");
                }

                @Override
                public void onUserAuthenticationFailed() {
                    Log.d("Authenticated", "false");
                }
            })));
            responseJson.put("successful", true);
        }

        responseJson.put("error", error);
        Log.d(LOG_TAG, responseJson.toString());
        return responseJson.toString();
    }

    private Response buildOkHtmlResponse(String responseBody) {
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, responseBody);
    }

    private Response builOkJsonResponse(String responseJson) {
        Response response = newFixedLengthResponse(Response.Status.OK, MIME_HTML, responseJson);
        response.addHeader("Set-Cookie", "pigeonId=123456789; Domain=" + serverIp);
        return response;
    }


    private String encode(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, CHARSET_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "ServerError";
        }
    }
}