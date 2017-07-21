package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import android.telephony.SmsManager;

import com.google.gson.JsonSyntaxException;
import com.jameswolfeoliver.pigeon.Models.ClientRequests.MessageRequest;
import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Models.Message;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.SqlWrappers.MessagesWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessagesEndpoint extends Endpoint {
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
        String postData = getPostData(session);
        if (postData == null) {
            return buildJsonError(-1, "Bad body", Status.BAD_REQUEST);
        }
        MessageRequest messageRequest;
        try {
            messageRequest = PigeonApplication.getGson().fromJson(postData, MessageRequest.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return buildJsonError(-1, "Bad body", Status.BAD_REQUEST);
        }

        // todo handle bad requests
        sendTextMessage(messageRequest.getConversation(), messageRequest.getMessage());

        /// todo give more meaningful response
        return buildJsonResponse("sentTime: " + System.currentTimeMillis(), Status.OK);
    }

    private static Response onGet(IHTTPSession session) {
        Map<String, List<String>> params = session.getParameters();
        String threadId = params.get("threadId").get(0);
        if (threadId == null) {
            return buildJsonError(-1, "threadId query required", Status.BAD_REQUEST);
        }
        long dateRangeStart = params.containsKey("dateRangeStart")
                ? Long.valueOf(params.get("dateRangeStart").get(0))
                : System.currentTimeMillis();
        MessagesWrapper messagesWrapper = new MessagesWrapper(threadId);
        List<Message> messages = messagesWrapper.find(() -> messagesWrapper.selectByDateRangeAndThread(dateRangeStart, threadId))
                .blockingFirst();
        return buildJsonResponse(PigeonApplication.getGson().toJson(messages), Status.OK);
    }


    private static void sendTextMessage(final Conversation conversation, String message) {
        ArrayList<String> messageParts = SmsManager.getDefault().divideMessage(message);
        SmsManager.getDefault().sendMultipartTextMessage(Long.toString(conversation.getAddress()), null, messageParts, null, null);
    }
}
