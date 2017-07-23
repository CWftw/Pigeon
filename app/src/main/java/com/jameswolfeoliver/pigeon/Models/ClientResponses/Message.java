package com.jameswolfeoliver.pigeon.Models.ClientResponses;

import com.jameswolfeoliver.pigeon.Models.Contact;
import com.jameswolfeoliver.pigeon.Models.Conversation;

public class Message {
    private Conversation conversation;
    private Contact contact;
    private long date;
    private String body;
    private long address;

    public void setDate(long date) {
        this.date = date;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
