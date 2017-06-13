package com.jameswolfeoliver.pigeon.Models.ClientResponses;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("successful")
    private boolean wasSuccessful;

    @SerializedName("error")
    private int errorCode;

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public LoginResponse(boolean wasSuccessful, int errorCode) {
        this.wasSuccessful = wasSuccessful;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return String.format("wasSuccessful: %b; errorCode: %d", wasSuccessful, errorCode);
    }
}
