package com.merchant.ops.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        if (endpoint == null || endpoint.isBlank()
                || accessKeyId == null || accessKeyId.isBlank()
                || accessKeySecret == null || accessKeySecret.isBlank()
                || bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("未配置阿里云 OSS 参数");
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException ex) {
            log.error("OSS 拒绝上传请求: code={}, requestId={}", ex.getErrorCode(), ex.getRequestId());
            throw new IllegalStateException("OSS 上传失败", ex);
        } catch (ClientException ex) {
            log.error("OSS 客户端调用失败", ex);
            throw new IllegalStateException("OSS 上传失败", ex);
        } finally {
            ossClient.shutdown();
        }

        String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
        log.info("文件已上传到 OSS: {}", objectName);
        return url;
    }
}