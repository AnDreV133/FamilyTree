package ru.andrev.notificationservice.service;

import ru.andrev.notificationservice.model.ChatMessage;
import ru.andrev.notificationservice.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    public ChatMessage sendMessage(Long consumerUserId, Long producerUserId, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConsumerUserId(consumerUserId);
        chatMessage.setProducerUserId(producerUserId);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setMessage(message);
        return chatRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessages(Long userId) {
        return chatRepository.findByConsumerUserIdOrProducerUserId(userId, userId);
    }

    public ChatMessage updateMessage(Long messageId, String newMessage) {
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setMessage(newMessage);
        return chatRepository.save(message);
    }

    public void deleteMessage(Long messageId) {
        chatRepository.deleteById(messageId);
    }
}