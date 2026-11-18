package com.merchant.ops.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 缓存失效消费者。删除操作天然幂等，重复消费不会产生额外副作用。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "merchant.rabbitmq.enabled", havingValue = "true")
public class CacheInvalidationListener {

    @Autowired
    private CacheInvalidationExecutor cacheInvalidationExecutor;

    @RabbitListener(queues = RabbitMqConfiguration.CACHE_QUEUE)
    public void handle(CacheInvalidationMessage message) {
        log.info("收到缓存失效消息: eventId={}, scope={}", message.getEventId(), message.getScope());
        cacheInvalidationExecutor.invalidate(message);
    }
}
