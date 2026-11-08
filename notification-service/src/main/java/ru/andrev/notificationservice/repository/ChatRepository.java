package ru.andrev.notificationservice.repository;

import ru.andrev.notificationservice.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConsumerUserIdOrProducerUserId(Long consumerUserId, Long producerUserId);
}