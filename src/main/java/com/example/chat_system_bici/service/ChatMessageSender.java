package com.example.chat_system_bici.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.chat_system_bici.config.RabbitMQConfig.EXCHANGE_NAME;
import static com.example.chat_system_bici.config.RabbitMQConfig.ROUTING_KEY;

@Service
public class ChatMessageSender {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ChatMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);
    }
}
