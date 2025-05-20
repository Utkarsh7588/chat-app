package com.example.chat_app.controller;

import com.example.chat_app.chat.ChatMessage;
import com.example.chat_app.chat.MessageType;
import com.example.chat_app.model.Messages;
import com.example.chat_app.repository.MessagesRepository;
import com.example.chat_app.repository.UsersRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessagesRepository messageRepository;
    private final UsersRepository usersRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          MessagesRepository messageRepository,
                          UsersRepository usersRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.usersRepository = usersRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage chatMessage, Message<?> message) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        int userId = (int) accessor.getSessionAttributes().get("userId");

        // Save to DB
        Messages dbMessage = new Messages();
        dbMessage.setContent(chatMessage.getContent());
        dbMessage.setSenderId(userId);
        dbMessage.setGroupId(chatMessage.getGroupId());
        dbMessage.setCreated(Instant.now());
        messageRepository.save(dbMessage);

        // Prepare and broadcast message
        chatMessage.setUsername(usersRepository.findNameById(userId));
        chatMessage.setUserId(userId);
        chatMessage.setType(MessageType.CHAT);
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getGroupId(), chatMessage);
    }

//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        int userId = (int) accessor.getSessionAttributes().get("userId");
//        String groupId = (String) accessor.getSessionAttributes().get("groupId");
//
//        ChatMessage connectMessage = new ChatMessage();
//        connectMessage.setType(MessageType.JOIN);
//        connectMessage.setUserId(userId);
//        connectMessage.setUsername(usersRepository.findNameById(userId));
//        connectMessage.setGroupId(groupId);
//        connectMessage.setContent(usersRepository.findNameById(userId) + " joined the chat!");
//
//        messagingTemplate.convertAndSend("/topic/" + groupId, connectMessage);
//    }

//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        int userId = (int) accessor.getSessionAttributes().get("userId");
//        String groupId = (String) accessor.getSessionAttributes().get("groupId");
//
//        ChatMessage disconnectMessage = new ChatMessage();
//        disconnectMessage.setType(MessageType.LEAVE);
//        disconnectMessage.setUserId(userId);
//        disconnectMessage.setUsername(usersRepository.findNameById(userId));
//        disconnectMessage.setGroupId(groupId);
//        disconnectMessage.setContent(usersRepository.findNameById(userId) + " left the chat!");
//
//        messagingTemplate.convertAndSend("/topic/" + groupId, disconnectMessage);
//    }
}