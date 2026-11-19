# 商家知识库 RAG 示例

当前只面向两类知识：

- `SOP`：门店操作流程。
- `POLICY`：平台规则和政策。

## 入库

```http
POST /admin/knowledge/documents
Authorization: <admin-token>
Content-Type: application/json
```

```json
{
  "tenantId": "tenant-demo",
  "storeId": "store-1",
  "docType": "POLICY",
  "title": "退款规则",
  "version": "2026.11",
  "effectiveFrom": "2026-11-01",
  "effectiveTo": "2027-12-31",
  "sourceUrl": "docs/rag/policy/refund.md",
  "content": "文档正文"
}
```

## 检索

```http
POST /admin/knowledge/search
Authorization: <admin-token>
Content-Type: application/json
```

```json
{
  "tenantId": "tenant-demo",
  "storeId": "store-1",
  "query": "顾客申请退款需要满足什么条件",
  "docType": "POLICY"
}
```

检索使用 Qdrant HNSW ANN，按 `tenantId`、`storeId`、`docType`、文档状态和生效日期过滤，再做 Top-K 和应用层重排。

## 示例文档

- `sop/order-timeout.md`
- `policy/refund.md`
- `policy/invoice.md`
- `policy/discount-stacking.md`
- `sop/store-closing.md`
