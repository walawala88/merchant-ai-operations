package com.merchant.ops.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存失效执行器。Redis 与 Spring Cache 共用该入口，便于 RabbitMQ 消费端和本地降级复用。
 */
@Slf4j
@Component
public class CacheInvalidationExecutor {

    public static final String SETMEAL_CACHE_NAME = "setmealCache";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    public void invalidate(CacheInvalidationMessage message) {
        if (message == null || message.getScope() == null) {
            return;
        }
        switch (message.getScope()) {
            case CacheInvalidationMessage.SCOPE_DISH:
                invalidateDish(message.getCategoryId());
                break;
            case CacheInvalidationMessage.SCOPE_SETMEAL:
                invalidateSetmeal();
                break;
            case CacheInvalidationMessage.SCOPE_ALL:
                invalidateDish(null);
                invalidateSetmeal();
                break;
            default:
                log.warn("未知缓存失效范围: {}", message.getScope());
        }
    }

    /**
     * categoryId 为空时清理全部 dish_*，否则只清理指定分类。
     */
    public void invalidateDish(Long categoryId) {
        if (categoryId == null) {
            Set keys = redisTemplate.keys("dish_*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            log.info("已清理全部菜品缓存");
            return;
        }
        redisTemplate.delete("dish_" + categoryId);
        log.info("已清理菜品缓存: dish_{}", categoryId);
    }

    public void invalidateSetmeal() {
        Cache cache = cacheManager.getCache(SETMEAL_CACHE_NAME);
        if (cache != null) {
            cache.clear();
        }
        log.info("已清理套餐缓存: {}", SETMEAL_CACHE_NAME);
    }
}
