package com.bervan.shstat.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test && !it")
public class QueueConfig {
    @Bean
    public Queue scraperQueue() {
        return new Queue("SCRAPER_QUEUE", true);
    }

    @Bean
    public DirectExchange scraperDirectExchange() {
        return new DirectExchange("SCRAPER_DIRECT_EXCHANGE");
    }

    @Bean
    public Binding scraperQueueBinding(Queue scraperQueue, DirectExchange scraperDirectExchange) {
        return BindingBuilder.bind(scraperQueue).to(scraperDirectExchange).with("SCRAPER_ROUTING_KEY");
    }

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
