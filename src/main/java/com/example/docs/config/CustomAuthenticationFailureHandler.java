package com.example.docs.config;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception.getMessage().equals("blocked")) {
            response.sendRedirect("/login?blocked");
        } else if (exception.getMessage().equals("notEnabled")) {
            response.sendRedirect("/login?notEnabled");
        } else if (exception.getMessage().equals("Bad credentials")) {
            response.sendRedirect("/login?error");
        }
    }
}


