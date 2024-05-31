package com.example.chat_system_bici.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.chat_system_bici.service.ChatMessageReceiver;

@Configuration
public class RabbitMQConfig {

	public static final String EXCHANGE_NAME = "chat-exchange";
	public static final String QUEUE_NAME = "chat-queue";
	public static final String ROUTING_KEY = "chat.message";
	
	@Bean
	Queue queue( ) {
		return new Queue (QUEUE_NAME, false);
	}
	
	@Bean
	TopicExchange exchange( ) {
		return new TopicExchange(EXCHANGE_NAME);
	}
	
	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder. bind(queue).to(exchange).with(ROUTING_KEY);
	}
	
	@Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
	
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
			                                                                    MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(QUEUE_NAME);
		container.setMessageListener(listenerAdapter);
		return container;
	}
	
	@Bean
    MessageListenerAdapter listenerAdapter(ChatMessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
	
}