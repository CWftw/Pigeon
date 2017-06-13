package com.jameswolfeoliver.pigeon.Models;


public class MessageInfo {
    private String reason;
    private boolean failed;

    public void fail(String reason) {
        this.reason = reason;
        failed = true;
    }

    public boolean isFailed() {
        return failed;
    }
}