package com.energy.monitoring.config;

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
    
    @Value("${rabbitmq.queue.device-data}")
    private String deviceDataQueue;
    
    @Value("${rabbitmq.queue.sync}")
    private String syncQueue;
    
    @Value("${rabbitmq.exchange.data}")
    private String dataExchange;
    
    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;
    
    @Value("${rabbitmq.routing-key.device-data}")
    private String deviceDataRoutingKey;
    
    @Value("${rabbitmq.routing-key.sync}")
    private String syncRoutingKey;

    @Value("${rabbitmq.exchange.notification:notification_exchange}")
    private String notificationExchange;
    
    // Queues
    @Bean
    public Queue deviceDataQueue() {
        return new Queue(deviceDataQueue, true);
    }
    
    @Bean
    public Queue syncQueue() {
        return new Queue(syncQueue, true);
    }
    
    // Exchanges
    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(dataExchange);
    }
    
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(notificationExchange);
    }
    
    // Bindings
    @Bean
    public Binding deviceDataBinding() {
        return BindingBuilder
            .bind(deviceDataQueue())
            .to(dataExchange())
            .with(deviceDataRoutingKey);
    }
    
    @Bean
    public Binding syncBinding() {
        return BindingBuilder
            .bind(syncQueue())
            .to(syncExchange())
            .with(syncRoutingKey);
    }
    
    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
