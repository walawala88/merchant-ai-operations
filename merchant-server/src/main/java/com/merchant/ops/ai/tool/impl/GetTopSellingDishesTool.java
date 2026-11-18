package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.service.ReportService;
import com.merchant.ops.vo.SalesTop10ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 查询热销菜品 Top10 工具
 * <p>
 * 复用 ReportService.getSalesTop10，不写 SQL。参数 begin/end 均为 yyyy-MM-dd，缺省时默认查询今天。
 */
@Component
public class GetTopSellingDishesTool implements AiTool {

    @Autowired
    private ReportService reportService;

    @Override
    public String getName() {
        return "getTopSellingDishes";
    }

    @Override
    public String getDescription() {
        return "查询指定日期区间销量最高的菜品 Top10，返回菜品名称列表与销量列表。参数 begin、end 为日期(yyyy-MM-dd)，缺省时默认查询今天。当用户询问卖得最好的菜、热门菜品、销量排行等时调用。";
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

        SalesTop10ReportVO vo = reportService.getSalesTop10(beginDate, endDate);
        return JSON.toJSONString(vo);
    }
}