package com.jameswolfeoliver.pigeon.Server.Endpoints.Login;

import com.jameswolfeoliver.pigeon.Server.Endpoint;
import com.jameswolfeoliver.pigeon.Server.Sessions.SessionManager;

public abstract class LoginEndpoint extends Endpoint {
    public LoginEndpoint(SessionManager sessionManager) {
        super(sessionManager);
    }
}
