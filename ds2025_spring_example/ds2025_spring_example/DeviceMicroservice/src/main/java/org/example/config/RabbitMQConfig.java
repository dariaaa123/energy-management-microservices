package org.example.config;

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
    
    @Value("${rabbitmq.queue.sync:synchronization_queue}")
    private String syncQueue;
    
    @Value("${rabbitmq.exchange.sync:sync_exchange}")
    private String syncExchange;
    
    @Value("${rabbitmq.routing-key.sync:sync.key}")
    private String syncRoutingKey;
    
    @Bean
    public Queue syncQueue() {
        return new Queue(syncQueue, true);
    }
    
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }
    
    @Bean
    public Binding syncBinding() {
        return BindingBuilder
            .bind(syncQueue())
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
