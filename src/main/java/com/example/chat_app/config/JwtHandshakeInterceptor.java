package com.example.chat_app.config;

import com.example.chat_app.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JWTService jwtService; // your custom service that validates and decodes the token

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            String groupId = servletRequest.getServletRequest().getParameter("groupId");// or from header
            if (token != null && jwtService.validateToken(token)) {
                int userId = jwtService.extractId(token);
                attributes.put("userId", userId);
                attributes.put("groupId",groupId);
                return true;// save to session
            }
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED); // optional, won't always reach client
        System.out.println("token not auth");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

}

