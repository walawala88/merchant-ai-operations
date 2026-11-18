package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.service.DishService;
import com.merchant.ops.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询菜品信息工具
 * <p>
 * 复用 DishService.listWithFlavor，不写 SQL。参数 name 为菜品名称（必填），支持模糊匹配。
 * 返回菜品详情（价格、状态、分类、口味等），用于回答“某菜品是否在售/多少钱”等问题。
 */
@Component
public class GetDishInfoTool implements AiTool {

    @Autowired
    private DishService dishService;

    @Override
    public String getName() {
        return "getDishInfo";
    }

    @Override
    public String getDescription() {
        return "查询菜品信息：价格、是否在售(status 1=起售 0=停售)、所属分类、口味等。参数 name 为菜品名称(必填)，支持模糊匹配。当用户询问某个具体菜品的价格、是否还能卖/是否在售、详情时调用。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");

        JSONObject properties = new JSONObject();
        properties.put("name", new JSONObject().fluentPut("type", "string").fluentPut("description", "菜品名称，必填，支持模糊匹配"));
        params.put("properties", properties);

        JSONArray required = new JSONArray();
        required.add("name");
        params.put("required", required);
        return params;
    }

    @Override
    public String execute(String arguments) {
        JSONObject args = JSON.parseObject(arguments);
        String name = args == null ? null : args.getString("name");
        if (name == null || name.isBlank()) {
            return JSON.toJSONString(new java.util.HashMap<String, Object>() {{
                put("error", "缺少参数 name（菜品名称）");
            }});
        }

        Dish query = new Dish();
        query.setName(name);
        List<DishVO> dishes = dishService.listWithFlavor(query);
        return JSON.toJSONString(dishes);
    }
}