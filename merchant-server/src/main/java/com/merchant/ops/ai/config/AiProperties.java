package com.merchant.ops.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 大模型相关配置
 */
@Component
@ConfigurationProperties(prefix = "merchant.ai")
@Data
public class AiProperties {

    /**
     * 大模型接口基础地址
     */
    private String baseUrl;

    /**
     * 大模型 API Key（通过环境变量注入，禁止硬编码）
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeoutMs = 30000;
}
