package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import android.telephony.SmsManager;

import com.google.gson.JsonSyntaxException;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import Models.Conversation;
import Models.ClientRequests.MessageRequest;
import com.jameswolfeoliver.pigeon.Server.TextServer;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MessagesEndpoint extends Endpoint {
    public static NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return onGet(session);
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
            return buildHtmlResponse(TextServer.getInternalError(), NanoHTTPD.Response.Status.INTERNAL_ERROR);
        }

        MessageRequest messageRequest;
        try {
            messageRequest = PigeonApplication.getGson().fromJson(bodyMap.get("postData"), MessageRequest.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return buildHtmlResponse(TextServer.getBadRequest(), NanoHTTPD.Response.Status.BAD_REQUEST);
        }

        // todo handle bad requests
        sendTextMessage(messageRequest.getConversation(), messageRequest.getMessage());

        /// todo give more meaningful response
        return buildJsonResponse("sentTime: " + System.currentTimeMillis(), NanoHTTPD.Response.Status.OK);
    }

    private static NanoHTTPD.Response onGet(NanoHTTPD.IHTTPSession session) {
        return buildHtmlResponse(TextServer.getForbidden(), NanoHTTPD.Response.Status.FORBIDDEN);
    }


    private static void sendTextMessage(final Conversation conversation, String message) {
        ArrayList<String> messageParts = SmsManager.getDefault().divideMessage(message);
        SmsManager.getDefault().sendMultipartTextMessage(Long.toString(conversation.getAddress()), null, messageParts, null, null);
    }
}
