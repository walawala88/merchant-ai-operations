package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.tool.AiTool;
import com.merchant.ops.ai.tool.PendingConfirmationStore;
import com.merchant.ops.entity.Dish;
import com.merchant.ops.service.DishService;
import com.merchant.ops.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 停售菜品工具（修改型，需要二次确认）
 * <p>
 * 安全机制：本工具不会直接停售。首次调用（无确认令牌）只查询并返回确认提示；
 * 用户明确确认后，LLM 携带 Java 生成的确认令牌再次调用，后端校验通过才真正执行
 * {@link DishService#startOrStop} 修改数据库。
 */
@Component
public class StopDishTool implements AiTool {

    @Autowired
    private DishService dishService;

    @Autowired
    private PendingConfirmationStore pendingConfirmationStore;

    @Override
    public String getName() {
        return "stopDish";
    }

    @Override
    public String getDescription() {
        return "将指定菜品设置为停售状态（修改型操作）。参数 name 为菜品名称(必填)。" +
                "本工具需要用户二次确认：首次调用返回确认提示和确认令牌，用户确认后携带令牌再次调用才会真正停售。" +
                "仅当用户明确要求停售某菜品时调用。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");

        JSONObject properties = new JSONObject();
        properties.put("name", new JSONObject().fluentPut("type", "string").fluentPut("description", "菜品名称，必填"));
        properties.put("token", new JSONObject().fluentPut("type", "string").fluentPut("description", "用户确认后由系统返回的确认令牌，首次调用不要携带"));
        params.put("properties", properties);

        com.alibaba.fastjson.JSONArray required = new com.alibaba.fastjson.JSONArray();
        required.add("name");
        params.put("required", required);
        return params;
    }

        @Override

        public boolean isWriteOperation() {

            return true;

        }

    

    @Override
    public String execute(String arguments) {
        JSONObject args = JSON.parseObject(arguments);
        String name = args == null ? null : args.getString("name");
        String token = args == null ? null : args.getString("token");
        if (name == null || name.isBlank()) {
            return JSON.toJSONString(buildError("缺少参数 name（菜品名称）"));
        }

        // 1. 校验菜品是否存在
        Dish query = new Dish();
        query.setName(name);
        List<DishVO> dishes = dishService.listWithFlavor(query);
        if (dishes == null || dishes.isEmpty()) {
            return JSON.toJSONString(buildError("未找到菜品：" + name + "，请确认菜品名称"));
        }
        DishVO dish = dishes.get(0);

        // 2. 校验当前状态，防止重复停售
        Integer status = dish.getStatus();
        if (status != null && status == 0) {
            return JSON.toJSONString(buildError("菜品「" + dish.getName() + "」当前已是停售状态，无需重复操作"));
        }

        // 3. 无确认令牌 → 只做确认提示，不执行修改
        if (token == null || token.isBlank()) {
            String pendingToken = pendingConfirmationStore.create(operationKey(dish.getId()));
            JSONObject result = new JSONObject();
            result.put("action", "CONFIRM_REQUIRED");
            result.put("token", pendingToken);
            result.put("dishName", dish.getName());
            result.put("currentStatus", "在售");
            result.put("message", "即将停售：「" + dish.getName() + "」，当前状态：在售。请向用户确认后再执行停售。");
            return result.toJSONString();
        }

        // 4. 携带确认令牌 → 校验后真正执行停售
        boolean confirmed = pendingConfirmationStore.validateAndConsume(token, operationKey(dish.getId()));
        if (!confirmed) {
            return JSON.toJSONString(buildError("确认令牌无效或已过期，请重新发起停售操作"));
        }
        dishService.startOrStop(0, dish.getId());

        JSONObject result = new JSONObject();
        result.put("action", "EXECUTED");
        result.put("dishName", dish.getName());
        result.put("message", "「" + dish.getName() + "」已停售。");
        return result.toJSONString();
    }

    /**
     * 待确认操作描述：以菜品 id 关联，防止名称变化导致误确认
     */
    private String operationKey(Long dishId) {
        return "STOP_DISH:" + dishId;
    }

    private JSONObject buildError(String message) {
        JSONObject error = new JSONObject();
        error.put("action", "ERROR");
        error.put("message", message);
        return error;
    }
}
