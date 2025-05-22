package com.example.chat_app.config;

import com.example.chat_app.chat.ChatMessage;
import com.example.chat_app.chat.MessageType;
import com.example.chat_app.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private UsersRepository usersRepository;
    private final SimpMessagingTemplate messagingTemplate;
    public WebSocketEventListener(UsersRepository usersRepository, SimpMessagingTemplate messagingTemplate){
        this.usersRepository=usersRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            int userId =(int) sessionAttributes.get("userId");
            String groupIdStr = (String) sessionAttributes.get("groupId");
            int groupId = Integer.parseInt(groupIdStr);
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent("User Joined");
            chatMessage.setUsername(usersRepository.findUsernameById(userId));
            chatMessage.setUserId(userId);
            chatMessage.setType(MessageType.JOIN);
            messagingTemplate.convertAndSend("/topic/" + groupId, chatMessage);
        } else {
            logger.warn("Connect event: No session attributes found.");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            int userId =(int) sessionAttributes.get("userId");
            String groupIdStr = (String) sessionAttributes.get("groupId");
            int groupId = Integer.parseInt(groupIdStr);
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setUsername(usersRepository.findUsernameById(userId));
            chatMessage.setContent("User left");
            chatMessage.setUserId(userId);
            chatMessage.setType(MessageType.LEAVE);
            messagingTemplate.convertAndSend("/topic/" + groupId, chatMessage);
        } else {
            logger.warn("Disconnect event: No session attributes found.");
        }
    }
}
