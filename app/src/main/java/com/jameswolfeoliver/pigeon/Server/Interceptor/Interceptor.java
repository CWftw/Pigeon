package com.jameswolfeoliver.pigeon.Server.Interceptor;


public interface Interceptor<T> {
    void intercept(T t);
}
