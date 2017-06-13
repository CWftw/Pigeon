package com.jameswolfeoliver.pigeon.Models.ClientRequests;

import com.jameswolfeoliver.pigeon.Models.Conversation;

/**
 * Created by james on 22/05/17.
 */

public class MessageRequest {
    Conversation conversation;
    String message;

    public Conversation getConversation() {
        return conversation;
    }

    public String getMessage() {
        return message;
    }
}
