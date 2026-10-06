package com.openlabmx.claudinary.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                       HttpServletResponse response, 
                       AuthenticationException authException) throws IOException, ServletException {
        
        String accept = request.getHeader("Accept");
        String path = request.getRequestURI();
        boolean isApiPath = path != null && path.startsWith("/api/");
        boolean wantsHtml = accept != null && accept.contains("text/html");

        if (wantsHtml && !isApiPath) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
    }
}
