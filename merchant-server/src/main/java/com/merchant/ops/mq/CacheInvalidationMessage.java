package com.merchant.ops.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存失效事件。生产端在数据库事务提交后发送，消费端执行幂等删除。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheInvalidationMessage {

    public static final String SCOPE_DISH = "DISH";
    public static final String SCOPE_SETMEAL = "SETMEAL";
    public static final String SCOPE_ALL = "ALL";

    private String eventId;
    private String scope;
    private Long categoryId;
    private String occurredAt;
}
