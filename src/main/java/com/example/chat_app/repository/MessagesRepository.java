package com.example.chat_app.repository;

import com.example.chat_app.model.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessagesRepository extends JpaRepository<Messages,Long> {
    List<Messages> findByGroupIdOrderByCreatedAsc(String groupId);
}
