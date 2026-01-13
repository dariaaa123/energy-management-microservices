package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${rabbitmq.exchange.sync:sync_exchange}")
    private String syncExchange;
    
    // Two separate queues for user synchronization (Device and Auth only)
    @Value("${rabbitmq.queue.sync.device:device_sync_queue}")
    private String deviceSyncQueue;
    
    @Value("${rabbitmq.queue.sync.auth:auth_sync_queue}")
    private String authSyncQueue;
    
    @Value("${rabbitmq.routing-key.sync:sync.key}")
    private String syncRoutingKey;
    
    // Device sync queue
    @Bean
    public Queue deviceSyncQueue() {
        return new Queue(deviceSyncQueue, true);
    }
    
    // Auth sync queue
    @Bean
    public Queue authSyncQueue() {
        return new Queue(authSyncQueue, true);
    }
    
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }
    
    // Bind both queues to the same exchange with the same routing key
    @Bean
    public Binding deviceSyncBinding() {
        return BindingBuilder
            .bind(deviceSyncQueue())
            .to(syncExchange())
            .with(syncRoutingKey);
    }
    
    @Bean
    public Binding authSyncBinding() {
        return BindingBuilder
            .bind(authSyncQueue())
            .to(syncExchange())
            .with(syncRoutingKey);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
