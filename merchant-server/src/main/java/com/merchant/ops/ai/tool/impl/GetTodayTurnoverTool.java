package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.service.WorkspaceService;
import com.merchant.ops.vo.BusinessDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 查询今日营业数据工具（复用 WorkspaceService 已有统计能力，不写 SQL）
 */
@Component
public class GetTodayTurnoverTool implements AiTool {

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public String getName() {
        return "getTodayTurnover";
    }

    @Override
    public String getDescription() {
        return "查询今天的营业数据，包括：营业额、有效订单数、订单完成率、平均客单价、新增用户数。当用户询问今天/今日的营业额、收入、订单等经营数据时调用。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");
        params.put("properties", new JSONObject());
        return params;
    }

    @Override
    public String execute(String arguments) {
        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        BusinessDataVO data = workspaceService.getBusinessData(begin, end);
        return JSON.toJSONString(data);
    }
}
