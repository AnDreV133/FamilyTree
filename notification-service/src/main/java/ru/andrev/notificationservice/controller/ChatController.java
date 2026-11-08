package ru.andrev.notificationservice.controller;

import ru.andrev.notificationservice.model.ChatMessage;
import ru.andrev.notificationservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping
    public ChatMessage sendMessage(
            @RequestParam Long consumerUserId,
            @RequestParam Long producerUserId,
            @RequestParam String message) {
        return chatService.sendMessage(consumerUserId, producerUserId, message);
    }

    @GetMapping
    public List<ChatMessage> getMessages(@RequestParam Long userId) {
        return chatService.getMessages(userId);
    }

    @PutMapping("/{messageId}")
    public ChatMessage updateMessage(
            @PathVariable Long messageId,
            @RequestParam String newMessage) {
        return chatService.updateMessage(messageId, newMessage);
    }

    @DeleteMapping("/{messageId}")
    public void deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
    }
}