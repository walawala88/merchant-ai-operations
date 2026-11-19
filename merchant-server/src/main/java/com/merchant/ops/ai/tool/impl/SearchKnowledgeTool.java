package com.merchant.ops.ai.tool.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import com.merchant.ops.ai.rag.service.KnowledgeContext;
import com.merchant.ops.ai.rag.service.KnowledgeRetrievalService;
import com.merchant.ops.ai.tool.AiTool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RAG tool for store SOP and platform policy knowledge.
 */
@Component
public class SearchKnowledgeTool implements AiTool {

    private final KnowledgeRetrievalService retrievalService;

    public SearchKnowledgeTool(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public String getName() {
        return "searchKnowledge";
    }

    @Override
    public String getDescription() {
        return "检索门店SOP和平台规则政策。用户询问退款、发票、活动叠加、配送、运营SOP或制度依据时调用。"
                + "返回内容包含文档标题、版本、生效日期、来源和片段，回答必须引用这些来源；没有检索结果时必须如实说明。";
    }

    @Override
    public JSONObject getParameters() {
        JSONObject params = new JSONObject();
        params.put("type", "object");
        JSONObject properties = new JSONObject();
        properties.put("query", new JSONObject()
                .fluentPut("type", "string")
                .fluentPut("description", "检索问题，保留用户原始业务表述"));
        properties.put("docType", new JSONObject()
                .fluentPut("type", "string")
                .fluentPut("description", "可选：SOP 或 POLICY，不确定时省略"));
        params.put("properties", properties);
        com.alibaba.fastjson.JSONArray required = new com.alibaba.fastjson.JSONArray();
        required.add("query");
        params.put("required", required);
        return params;
    }

    @Override
    public String execute(String arguments) {
        JSONObject args = JSON.parseObject(arguments);
        String query = args == null ? null : args.getString("query");
        String docType = args == null ? null : args.getString("docType");
        String tenantId = KnowledgeContext.tenantId();
        String storeId = KnowledgeContext.storeId();
        if (tenantId == null || tenantId.isBlank()) {
            return JSON.toJSONString(java.util.Map.of("error", "缺少租户上下文，无法检索知识库"));
        }
        List<KnowledgeSearchHit> hits = retrievalService.search(tenantId, storeId, query, docType);
        return JSON.toJSONString(hits);
    }
}

