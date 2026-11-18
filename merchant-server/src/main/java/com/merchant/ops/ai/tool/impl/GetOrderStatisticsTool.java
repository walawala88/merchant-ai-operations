package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 查询订单统计工具
 * <p>
 * 复用 ReportService.getOrdersStatistics，不写 SQL。参数 begin/end 均为 yyyy-MM-dd，
 * 缺省时默认查询最近 7 天。
 */
@Component
public class GetOrderStatisticsTool implements AiTool {

    @Autowired
    private ReportService reportService;

    @Override
    public String getName() {
        return "getOrderStatistics";
    }

    @Override
    public String getDescription() {
        return "查询指定日期区间的订单统计，包括：每日订单总数、每日有效订单数、区间总订单数、有效订单数、订单完成率。参数 begin、end 为日期(yyyy-MM-dd)，缺省时默认查询最近 7 天。当用户询问订单情况、订单量、订单数量、完成率等时调用。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");

        JSONObject properties = new JSONObject();
        properties.put("begin", new JSONObject().fluentPut("type", "string").fluentPut("description", "开始日期 yyyy-MM-dd，可省略，默认最近7天的起始日"));
        properties.put("end", new JSONObject().fluentPut("type", "string").fluentPut("description", "结束日期 yyyy-MM-dd，可省略，默认今天"));
        params.put("properties", properties);
        return params;
    }

    @Override
    public String execute(String arguments) {
        LocalDate beginDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();
        if (arguments != null && !arguments.isBlank()) {
            JSONObject args = JSON.parseObject(arguments);
            if (args.getString("begin") != null) {
                beginDate = LocalDate.parse(args.getString("begin"));
            }
            if (args.getString("end") != null) {
                endDate = LocalDate.parse(args.getString("end"));
            }
        }

        Object result = reportService.getOrdersStatistics(beginDate, endDate);
        return JSON.toJSONString(result);
    }
}