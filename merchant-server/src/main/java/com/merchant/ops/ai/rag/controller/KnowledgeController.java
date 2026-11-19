package com.merchant.ops.ai.rag.controller;

import com.merchant.ops.ai.rag.dto.KnowledgeDocumentRequest;
import com.merchant.ops.ai.rag.dto.KnowledgeIngestionResult;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchHit;
import com.merchant.ops.ai.rag.dto.KnowledgeSearchRequest;
import com.merchant.ops.ai.rag.service.KnowledgeIngestionService;
import com.merchant.ops.ai.rag.service.KnowledgeRetrievalService;
import com.merchant.ops.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/knowledge")
@Api(tags = "知识库 RAG")
public class KnowledgeController {

    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeController(KnowledgeIngestionService ingestionService,
                               KnowledgeRetrievalService retrievalService) {
        this.ingestionService = ingestionService;
        this.retrievalService = retrievalService;
    }

    @PostMapping("/documents")
    @ApiOperation("导入或更新 SOP/政策文档")
    public Result<KnowledgeIngestionResult> ingest(@RequestBody KnowledgeDocumentRequest request) {
        return Result.success(ingestionService.ingest(request));
    }

    @PostMapping("/search")
    @ApiOperation("按租户和门店检索 SOP/政策")
    public Result<List<KnowledgeSearchHit>> search(@RequestBody KnowledgeSearchRequest request) {
        return Result.success(retrievalService.search(
                request.getTenantId(), request.getStoreId(), request.getQuery(), request.getDocType()));
    }
}
