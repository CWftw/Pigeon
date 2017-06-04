package com.jameswolfeoliver.pigeon.SqlWrappers;

public interface Wrapper<T> {
    String SELECTION_BUNDLE_KEY = "selection";

    void get(int callerId, SqlCallback<T> callback, String... args);
    void find(int callerId, SqlCallback<T> callback, String... args);
}
