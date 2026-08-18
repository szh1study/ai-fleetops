\# AI FleetOps



\*\*Robotaxi 智能车队运营平台 / AI Incident Copilot\*\*



AI FleetOps 是一个持续迭代的 Robotaxi 车队运营与 AI 辅助分析项目。



项目采用分版本开发方式：V0.1 先完成车辆、异常事件、维修工单等基础运营能力，建立稳定的业务数据和状态流转；V0.2 在此基础上开发 AI Incident Copilot，将大模型能力接入真实业务场景。



\## Project Status



\* ✅ \*\*V0.1 — Fleet Operations Foundation：已完成\*\*

\* 🚧 \*\*V0.2 — AI Incident Copilot：开发中\*\*

\* 📌 V0.3 — RAG + Tool Calling + Human-in-the-loop：规划中



> 当前仓库明确区分已完成功能与开发中功能，不将 Roadmap 中的功能作为现有能力展示。



\---

## Project Screenshots

### Dashboard / 运营总览

![Dashboard](docs/screenshots/01-dashboard.png)

### Fleet Management / 车队管理

![Fleet Management](docs/screenshots/02-fleet-management.png)

### Incident Center / 异常中心

![Incident Center](docs/screenshots/03-incident-center.png)

### Work Order Center / 工单中心

![Work Order Center](docs/screenshots/04-work-order-center.png)

---

\## Core Features



\### V0.1 — Fleet Operations Foundation



目前已实现：



\* Dashboard 运营总览

\* Robotaxi 车辆管理

\* Incident 异常事件管理

\* Work Order 维修工单管理

\* Vehicle 状态管理

\* Device Status 独立更新

\* REST API 前后端通信

\* Incident / Work Order 生命周期管理

\* 前端运营页面与数据可视化



\### V0.2 — AI Incident Copilot



当前正在开发：



\* AI Model Provider 接入

\* Incident AI 分析接口

\* Incident / Vehicle 业务上下文构建

\* AI 服务不可用时的降级处理

\* 阿里云百炼 / Qwen 模型 API 接入



后续计划：



\* 结构化 AI 异常分析

\* SOP / RAG

\* Tool Calling

\* Human-in-the-loop



\---



\## Tech Stack



\### Backend



\* Java

\* Spring Boot

\* MyBatis-Plus

\* MySQL

\* REST API



\### Frontend



\* Vue 3

\* Vite

\* Axios

\* Element Plus

\* ECharts



\### AI



\* AI Provider Abstraction

\* Aliyun Bailian / Qwen API（V0.2 开发中）

\* RAG / Tool Calling（Roadmap）



\---



\## System Architecture



```text

Vue 3 Frontend

&#x20;      │

&#x20;      │ Axios / REST API

&#x20;      ▼

Spring Boot Controller

&#x20;      │

&#x20;      ▼

&#x20;    Service

&#x20;      │

&#x20;      ▼

&#x20;    Mapper

&#x20;      │

&#x20;      ▼

&#x20;     MySQL

```



V0.2 AI 链路：



```text

Incident / Vehicle

&#x20;       │

&#x20;       ▼

AI Incident Copilot

&#x20;       │

&#x20;       ▼

Model Provider

&#x20;       │

&#x20;       ▼

Aliyun Bailian / Qwen

```



\---



\## Demo Workflow



项目 V0.1 已建立一套完整的异常处置业务闭环：



```text

Camera OFFLINE

&#x20;     ↓

Incident Created

&#x20;     ↓

PENDING\_APPROVAL

&#x20;     ↓

PROCESSING

&#x20;     ↓

Create Work Order

&#x20;     ↓

Work Order COMPLETED

&#x20;     ↓

Incident RESOLVED

&#x20;     ↓

Camera NORMAL

&#x20;     ↓

Vehicle READY

```



该流程用于模拟 Robotaxi 运营过程中设备异常、维修处理与车辆恢复的完整生命周期。



\---



\## Engineering Highlights



项目开发过程中重点记录并处理真实工程问题，而不仅仅以“代码能够运行”为目标。



\### 1. Vehicle / Device 状态一致性



曾出现：



```text

Vehicle = READY

Camera = OFFLINE

```



程序本身没有报错，但业务状态存在明显不一致。



排查后将车辆状态和设备状态更新逻辑进行拆分，并增加独立的 Device Status 更新接口，使两类状态能够按照业务流程分别维护。



\### 2. 生命周期字段完整性



针对状态流转过程中时间字段缺失的问题，对业务逻辑进行补充：



\* Incident → `RESOLVED` 时维护 `resolvedAt`

\* Work Order → `COMPLETED` 时维护 `completedAt`



\### 3. 开发环境问题排查



项目开发过程中实际处理过：



\* JDK 版本不匹配

\* Maven PATH 配置问题

\* 8080 端口占用

\* MySQL Access Denied

\* 数据库配置与环境变量管理



\---



\## Getting Started



\### Requirements



建议环境：



\* JDK 17+

\* Maven

\* MySQL

\* Node.js / npm



\### Database Configuration



项目使用环境变量管理数据库凭证，不在仓库中保存真实数据库密码。



Windows PowerShell 示例：



```powershell

$env:DB\_USERNAME="root"

$env:DB\_PASSWORD="YOUR\_PASSWORD"

```



请根据本地环境创建并初始化对应的 MySQL 数据库。



\### Backend



进入项目根目录：



```bash

mvn spring-boot:run

```



默认后端地址：



```text

http://localhost:8080

```



\### Frontend



进入：



```bash

cd frontend

```



安装依赖：



```bash

npm install

```



启动：



```bash

npm run dev

```



默认开发环境访问地址通常为：



```text

http://localhost:5173

```



\---



\## Roadmap



| Version | Direction                              | Status         |

| ------- | -------------------------------------- | -------------- |

| V0.1    | Fleet Operations Foundation            | ✅ DONE         |

| V0.2    | AI Incident Copilot                    | 🚧 IN PROGRESS |

| V0.3    | RAG + Tool Calling + Human-in-the-loop | Planned        |

| V0.4    | Daily Brief + Weather / Map / Forecast | Planned        |

| V0.5    | Daily Review \& Data Loop               | Planned        |

| V1.0    | Public Deployment + Demo               | Planned        |



\---



\## Project Type



\*\*Personal Project / 个人持续迭代项目\*\*



本项目用于持续实践：



\* 软件工程与业务建模

\* REST API 开发

\* 前后端联调

\* Debug 与异常处理

\* AI Model API Integration

\* Agent / Tool Calling 工程化方向



目前项目仍在持续开发中。



