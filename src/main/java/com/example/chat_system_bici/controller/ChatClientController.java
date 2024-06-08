package com.example.chat_system_bici.controller;

import com.example.chat_system_bici.model.ChatClient;
import com.example.chat_system_bici.service.ChatMessageSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/chat")
public class ChatClientController {

    private static final Logger logger = LoggerFactory.getLogger(ChatClientController.class);
    private final ChatMessageSender chatMessageSender;

    @Autowired
    public ChatClientController(ChatMessageSender chatMessageSender) {
        this.chatMessageSender = chatMessageSender;
    }

    private ChatClient chatClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostMapping("/connect")
    public ResponseEntity<String> connect(@RequestParam String serverName, @RequestParam int serverPort) {
        if (chatClient != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Already connected to a chat server");
        }

        chatClient = new ChatClient(serverName, serverPort);
        executorService.submit(() -> {
            try {
                chatClient.start();
                logger.info("Connected to chat server at {}:{}", serverName, serverPort);
            } catch (IOException e) {
                logger.error("Error connecting to chat server", e);
            }
        });
        return ResponseEntity.ok("Connected to chat server");
    }

    @PostMapping("/disconnect")
    public ResponseEntity<String> disconnect() {
        if (chatClient == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not connected to any chat server");
        }

        try {
            chatClient.stop();
            chatClient = null;
            logger.info("Disconnected from chat server");
            return ResponseEntity.ok("Disconnected from chat server");
        } catch (IOException e) {
            logger.error("Error disconnecting from chat server", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error disconnecting from chat server");
        } finally {
            executorService.shutdown();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message cannot be null or empty");
        }

        try {
            chatMessageSender.sendMessage(message);
            logger.info("Message sent: {}", message);
            return ResponseEntity.ok("Message sent");
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message");
        }
    }
}
