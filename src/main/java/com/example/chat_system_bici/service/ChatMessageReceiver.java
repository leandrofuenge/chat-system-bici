package com.example.chat_system_bici.service;

import org.springframework.stereotype.Service;

@Service
public class ChatMessageReceiver {

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        // Process the message (e.g., broadcast to connected clients)
    }
}
