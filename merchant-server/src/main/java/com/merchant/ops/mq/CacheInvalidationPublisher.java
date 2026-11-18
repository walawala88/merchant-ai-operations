package com.merchant.ops.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 缓存失效事件发布器。
 *
 * RabbitMQ 开启时：在事务提交后发送消息，由消费者删除缓存。
 * RabbitMQ 关闭或发送失败时：直接执行本地缓存删除，保证功能可用。
 */
@Slf4j
@Component
public class CacheInvalidationPublisher {

    @Value("${merchant.rabbitmq.enabled:false}")
    private boolean rabbitEnabled;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CacheInvalidationExecutor cacheInvalidationExecutor;

    public void publishDishChanged(Long categoryId) {
        publish(CacheInvalidationMessage.SCOPE_DISH, categoryId);
    }

    public void publishAllChanged() {
        publish(CacheInvalidationMessage.SCOPE_ALL, null);
    }

    public void publishSetmealChanged() {
        publish(CacheInvalidationMessage.SCOPE_SETMEAL, null);
    }

    private void publish(String scope, Long categoryId) {
        CacheInvalidationMessage message = new CacheInvalidationMessage(
                UUID.randomUUID().toString(),
                scope,
                categoryId,
                LocalDateTime.now().toString()
        );

        Runnable dispatch = () -> dispatch(message);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch.run();
                }
            });
        } else {
            dispatch.run();
        }
    }

    private void dispatch(CacheInvalidationMessage message) {
        if (!rabbitEnabled) {
            log.debug("RabbitMQ 未启用，使用本地缓存失效: {}", message.getScope());
            cacheInvalidationExecutor.invalidate(message);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfiguration.CACHE_EXCHANGE,
                    RabbitMqConfiguration.CACHE_ROUTING_KEY,
                    message
            );
            log.info("缓存失效消息已发送: eventId={}, scope={}", message.getEventId(), message.getScope());
        } catch (AmqpException ex) {
            log.error("RabbitMQ 缓存失效消息发送失败，执行本地降级", ex);
            cacheInvalidationExecutor.invalidate(message);
        }
    }
}
