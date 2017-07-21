package com.jameswolfeoliver.pigeon.Models.ClientResponses;


public class Error {
    private int errorCode;
    private String error;
    public Error(int errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }
}
