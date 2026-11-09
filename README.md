# 外卖点餐系统 + AI 智能点餐 Agent

基于 Spring Boot 的外卖点餐系统，并扩展了基于大模型的 AI 智能点餐 Agent，支持自然语言点餐、经营分析等能力。Java 承载核心业务，Python 负责 Agent 编排与模型服务。

## 技术栈
- Java 后端：Spring Boot、MyBatis、Druid、MySQL、Redis、WebSocket
- 支付 / 存储：微信支付、阿里云 OSS
- AI 服务：Python + FastAPI + LangGraph + DeepSeek-R1 + Ollama + RAG
- 监控：Prometheus

## 核心功能
- 员工 / 用户登录：JWT 鉴权
- 菜品 / 套餐管理、购物车、订单、微信支付
- WebSocket 来单提醒与催单、定时任务处理超时订单
- 经营报表：营业额、用户、订单统计
- 阿里云 OSS 文件上传
- AI 智能点餐：自然语言点餐、经营分析、异常识别（Agent 编排）

## 核心难点
- Java + Python 双栈协同：Java 侧实现「权限受控 Tool Gateway」，隔离模型推理与数据库访问，避免大模型直接触库
- LangGraph 多角色 Agent：身份 / 意图路由、任务规划、工具调用、结果校验与失败重试
- 轻量 RAG 知识库 + 人工确认（Human-in-the-loop）
- 结构化 JSON + 字段校验提升模型调用稳定性

## 如何运行
1. 环境要求：JDK、Maven、MySQL 8、Redis、Python 3.10+、Ollama
2. 初始化数据库 `sky_take_out`（表结构见 `sky-server/src/main/resources/mapper/*.xml`）
3. 修改 `sky-server/src/main/resources/application-dev.yml` 中的数据库 / Redis / 微信支付 / 阿里云 OSS 配置（可参考 `application-dev.yml.example`）
4. 启动 Java 后端：`mvn spring-boot:run`
5. 启动 AI 服务（独立项目 `ai-agent-service`）：`uvicorn app:app --port 8001`
6. 通过 Knife4j 查看接口文档

## 关联项目
- AI Agent 服务（Python 端）：https://github.com/JIANGjixuan111/dining-agent-service
