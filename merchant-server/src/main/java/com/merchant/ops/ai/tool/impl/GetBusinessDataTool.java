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
 * 查询指定日期区间经营数据工具（营业额/有效订单/完成率/客单价/新增用户）
 * <p>
 * 复用 WorkspaceService.getBusinessData，不写 SQL。参数 begin/end 均为 yyyy-MM-dd，缺省时默认查询今天。
 */
@Component
public class GetBusinessDataTool implements AiTool {

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public String getName() {
        return "getBusinessData";
    }

    @Override
    public String getDescription() {
        return "查询指定日期区间的经营数据，包括：营业额、有效订单数、订单完成率、平均客单价、新增用户数。参数 begin、end 为日期(yyyy-MM-dd)，缺省时默认查询今天。当用户询问某一天或某段日期的营业额、订单、经营概况时调用。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");

        JSONObject properties = new JSONObject();
        properties.put("begin", new JSONObject().fluentPut("type", "string").fluentPut("description", "开始日期 yyyy-MM-dd，可省略，默认今天"));
        properties.put("end", new JSONObject().fluentPut("type", "string").fluentPut("description", "结束日期 yyyy-MM-dd，可省略，默认今天"));
        params.put("properties", properties);
        return params;
    }

    @Override
    public String execute(String arguments) {
        LocalDate beginDate = LocalDate.now();
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

        LocalDateTime begin = beginDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        BusinessDataVO data = workspaceService.getBusinessData(begin, end);
        return JSON.toJSONString(data);
    }
}