package com.jameswolfeoliver.pigeon.Server.Sessions;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Session extends RealmObject {
    @PrimaryKey
    private int id;

    private String name;
    private String value;
    private boolean isSessionValid;
    private long issueTimestamp;
    private long expiryTimestamp;

    public Session() {}

    public Session(@NonNull String name, @NonNull String value) {
        this.name = name;
        this.value = value;
        this.id = hashCode();
    }

    public int getId() {
        return id;
    }

    @Deprecated
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public boolean isSessionValid() {
        return isSessionValid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getIssueTimestamp() {
        return issueTimestamp;
    }

    public void setIssueTimestamp(long issueTimestamp) {
        this.issueTimestamp = issueTimestamp;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public void setSessionValid(boolean isSessionValid) {
        this.isSessionValid = isSessionValid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (!name.equals(session.name)) return false;
        return value.equals(session.value);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", isSessionValid=" + isSessionValid +
                ", issueTimestamp=" + issueTimestamp +
                ", expiryTimestamp=" + expiryTimestamp +
                '}';
    }
}
