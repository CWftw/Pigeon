package com.jameswolfeoliver.pigeon.Server.Models;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Conversation {
    @SerializedName("person")
    private int person;

    @SerializedName("date")
    private long date;

    @SerializedName("threadId")
    private int threadId;

    @SerializedName("address")
    private long address;

    @SerializedName("type")
    private int type;

    @SerializedName("status")
    private int status;

    @SerializedName("read")
    private int read;
    
    @SerializedName("snippet")
    private String snippet;

    private Conversation(int threadId) {
        this.threadId = threadId;
    }

    public int getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public int getThreadId() {
        return threadId;
    }

    public long getAddress() {
        return address;
    }

    public int getPerson() {
        return person;
    }

    public String getSnippet() {
        return snippet;
    }

    public boolean getRead() {
        return read == 1;
    }

    public int getStatus() {
        return status;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("person: %s; threadId: %d; address: %s; date: %d;" +
                "\n\tsnippet: %s; " +
                "\n\t\ttype: %d; status: %d, read: %b;",
                person, threadId, address, date, snippet, type, status, getRead());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Conversation)) {
            return false;
        }
        Conversation conversation = (Conversation) object;
        return Objects.equals(this.threadId, conversation.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadId);
    }

    public static class Builder {
        private Conversation conversation;

        public Builder(int threadId) {
            this.conversation = new Conversation(threadId);
            this.conversation.address = 0;
            this.conversation.person = 0;
            this.conversation.date = 0;
            this.conversation.type = -1;
            this.conversation.snippet = "";
            this.conversation.status = -1;
            this.conversation.read = 0;
        }

        public Builder setSnippet(String snippet) {
            if (snippet != null)
                this.conversation.snippet = snippet;
            return this;
        }

        public Builder setPerson(int person) {
            this.conversation.person = person;
            return this;
        }

        public Builder setAddress(long address) {
            this.conversation.address = address;
            return this;
        }

        public Builder setDate(long date) {
            this.conversation.date = date;
            return this;
        }

        public Builder setThreadId(int threadId) {
            this.conversation.threadId = threadId;
            return this;
        }

        public Builder setType(int type) {
            this.conversation.type = type;
            return this;
        }

        public Builder setRead(int read) {
            this.conversation.read = read;
            return this;
        }

        public Builder setStatus(int status) {
            this.conversation.status = status;
            return this;
        }
        
        public Conversation build() {
            return conversation;
        }
    }
}
