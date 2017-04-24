package com.jameswolfeoliver.pigeon.SqlWrappers;

public interface Wrapper<T> {
    void get(int callerId, SqlCallback<T> callback, String... args);
    void find(int callerId, SqlCallback<T> callback, String... args);
}
