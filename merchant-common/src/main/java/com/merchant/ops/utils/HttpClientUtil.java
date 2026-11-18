package com.merchant.ops.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class HttpClientUtil {

    private static final int TIMEOUT_MSEC = 5 * 1000;

    private HttpClientUtil() {
    }

    public static String doGet(String url, Map<String, String> paramMap) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                paramMap.forEach(builder::addParameter);
            }
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ex) {
            log.error("HTTP GET 请求失败: {}", url, ex);
        }
        return "";
    }

    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                paramMap.forEach((key, value) -> paramList.add(new BasicNameValuePair(key, value)));
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8));
            }
            httpPost.setConfig(builderRequestConfig());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }

    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            if (paramMap != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.putAll(paramMap);
                StringEntity entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
                entity.setContentEncoding(StandardCharsets.UTF_8.name());
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            httpPost.setConfig(builderRequestConfig());
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        }
    }

    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC)
                .build();
    }
}