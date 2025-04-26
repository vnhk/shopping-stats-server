package com.bervan.shstat.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Bean
    public Queue productsQueue() {
        return new Queue("PRODUCTS_QUEUE", true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("DIRECT_EXCHANGE");
    }

    @Bean
    public Binding productsQueueBinding(Queue productsQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(productsQueue).to(directExchange).with("PRODUCTS_ROUTING_KEY");
    }
}
