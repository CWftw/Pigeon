package com.jameswolfeoliver.pigeon.Server.Models;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Message {
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

    @SerializedName("body")
    private String body;

    private Message(int threadId) {
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

    public String getBody() {
        return body;
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
                        "\n\tbody: %s; " +
                        "\n\t\ttype: %d; status: %d, read: %b;",
                person, threadId, address, date, body, type, status, getRead());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Conversation)) {
            return false;
        }
        Message message = (Message) object;
        return Objects.equals(this.threadId, message.threadId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(threadId, body);
    }

    public static class Builder {
        private Message message;

        public Builder(int threadId) {
            this.message = new Message(threadId);
            this.message.address = 0;
            this.message.person = 0;
            this.message.date = 0;
            this.message.type = -1;
            this.message.body = "";
            this.message.status = -1;
            this.message.read = 0;
        }

        public Builder setBody(String body) {
            if (body != null)
                this.message.body = body;
            return this;
        }

        public Builder setPerson(int person) {
            this.message.person = person;
            return this;
        }

        public Builder setAddress(long address) {
            this.message.address = address;
            return this;
        }

        public Builder setDate(long date) {
            this.message.date = date;
            return this;
        }

        public Builder setThreadId(int threadId) {
            this.message.threadId = threadId;
            return this;
        }

        public Builder setType(int type) {
            this.message.type = type;
            return this;
        }

        public Builder setRead(int read) {
            this.message.read = read;
            return this;
        }

        public Builder setStatus(int status) {
            this.message.status = status;
            return this;
        }

        public Message build() {
            return message;
        }
    }
}
