package com.jameswolfeoliver.pigeon.Models.ClientResponses;


public class Error {
    private int errorCode;
    private String error;
    public Error(int errorCode, String error) {
        this.errorCode = errorCode;
        this.error = error;
    }

    public static final class Codes {
        public static final int MY_FAULT = -2;
        public static final int YOUR_FAULT = -1;
    }
}
