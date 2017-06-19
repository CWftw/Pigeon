package com.jameswolfeoliver.pigeon.Models.ClientRequests;


import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("password")
    private String password;

    @SerializedName("hasAgreedToTerms")
    private boolean userHasAgreed;

    public String getDeviceName() {
        return deviceName;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasUserAgreed() {
        return userHasAgreed;
    }
}
