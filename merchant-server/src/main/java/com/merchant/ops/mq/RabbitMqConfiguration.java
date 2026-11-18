package com.merchant.ops.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 缓存失效拓扑。
 *
 * 主队列失败重试耗尽后进入死信队列，避免消息直接丢失。
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "merchant.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfiguration {

    public static final String CACHE_EXCHANGE = "merchant.cache.exchange";
    public static final String CACHE_QUEUE = "merchant.cache.invalidation.queue";
    public static final String CACHE_ROUTING_KEY = "merchant.cache.invalidation";
    public static final String DEAD_LETTER_EXCHANGE = "merchant.cache.dlx";
    public static final String DEAD_LETTER_QUEUE = "merchant.cache.invalidation.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "merchant.cache.invalidation.dead";

    @Bean
    public DirectExchange cacheInvalidationExchange() {
        return new DirectExchange(CACHE_EXCHANGE, true, false);
    }

    @Bean
    public Queue cacheInvalidationQueue() {
        return QueueBuilder.durable(CACHE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding cacheInvalidationBinding() {
        return BindingBuilder.bind(cacheInvalidationQueue())
                .to(cacheInvalidationExchange())
                .with(CACHE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange cacheDeadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue cacheDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding cacheDeadLetterBinding() {
        return BindingBuilder.bind(cacheDeadLetterQueue())
                .to(cacheDeadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
