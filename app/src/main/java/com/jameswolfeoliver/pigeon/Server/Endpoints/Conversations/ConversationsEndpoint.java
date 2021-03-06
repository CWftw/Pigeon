package com.jameswolfeoliver.pigeon.Server.Endpoints.Conversations;

import com.jameswolfeoliver.pigeon.Models.Conversation;
import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.PigeonServer;
import com.jameswolfeoliver.pigeon.SqlWrappers.ConversationWrapper;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConversationsEndpoint extends Endpoint {
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
        return buildJsonError(-2, "In development", Status.NOT_IMPLEMENTED);
    }

    private static Response onGet(IHTTPSession session) {
        ConversationWrapper conversationWrapper = new ConversationWrapper();
        Iterable<Conversation> conversations = conversationWrapper.fetch()
                .blockingIterable();
        List<Conversation> conversationList = new ArrayList<>();
        Iterator<Conversation> conversationIterator = conversations.iterator();
        while (conversationIterator.hasNext()) conversationList.add(conversationIterator.next());
        return buildJsonResponse(PigeonApplication.getGson().toJson(conversationList), Status.OK);
    }
}
