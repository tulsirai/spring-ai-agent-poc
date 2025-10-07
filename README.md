# 🧠 TRai’s AI Agent — Spring Boot + Spring AI + OpenAI Demo

> A practical **Agentic AI** proof-of-concept built with **Spring Boot** and **Spring AI**, demonstrating how Java developers can create intelligent agents that reason, plan, and call backend functions (tools) such as querying, creating, or deleting orders.

---

## 🚀 Overview

**TRai’s AI Agent** shows how a Large Language Model (LLM) can act as an *intelligent interface* to your existing systems — performing dynamic reasoning and tool execution instead of following static, rule-based logic.

It integrates:

- 🪶 **Spring Boot 3.x**
- 🧩 **Spring AI** (LLM abstraction & agent orchestration)
- 🤖 **OpenAI GPT model** (can be swapped for Azure OpenAI or local Llama)
- 🗃️ **JPA/Hibernate** for persistence
- ⚙️ **Agentic Tools** registered as functions callable by the LLM

The agent can:
- Create, fetch, and list orders.
- Retrieve counts and filter by status.
- Soft-delete orders safely with confirmation & audit.
- Maintain conversational memory (session-based).

---

## 🧱 Architecture

```text
┌────────────────────────────────────────────────────────────┐
│                        User / UI                           │
│         (React chat frontend or Postman client)            │
└────────────────────────────────────────────────────────────┘
                 │ POST /api/agent/chat
                 ▼
┌────────────────────────────────────────────────────────────┐
│                  AgentController (REST)                    │
│ - Handles incoming messages                                │
│ - Detects greetings for deterministic responses            │
└────────────────────────────────────────────────────────────┘
                 ▼
┌────────────────────────────────────────────────────────────┐
│                    AgentService                            │
│ - Wraps Spring AI ChatClient                               │
│ - Injects memory + system prompts                          │
│ - Registers @Tool methods from ToolConfig                  │
└────────────────────────────────────────────────────────────┘
                 ▼
┌────────────────────────────────────────────────────────────┐
│                       ToolConfig                           │
│ - Annotated @Tool methods for:                             │
│   • create_order                                           │
│   • get_order_status                                       │
│   • orders_for_customer                                    │
│   • orders_by_status                                       │
│   • count_orders                                           │
│   • delete_order (with confirmation + reason)              │
│ - Uses JPA repository to perform real DB operations        │
└────────────────────────────────────────────────────────────┘
                 ▼
┌────────────────────────────────────────────────────────────┐
│                    OrderJpaRepository                      │
│ - JPA CRUD interface for Order entity                      │
│ - Supports queries by customer, status, etc.               │
└────────────────────────────────────────────────────────────┘
                 ▼
┌────────────────────────────────────────────────────────────┐
│                        H2 / SQL DB                         │
└────────────────────────────────────────────────────────────┘
```

---

## 🧠 Agentic Behavior

The Agent relies on the LLM to decide **when** and **how** to invoke your Java tools:

| User Input | Agent Reasoning | Tool Invoked |
|-------------|-----------------|---------------|
| “Create an order for customer `tulsi` with status `NEW`.” | Needs to persist an order | `create_order` |
| “Show me all orders for `suprada`.” | Retrieve by customer ID | `orders_for_customer` |
| “Delete order O-123 because it’s duplicate.” | Must confirm deletion first | `delete_order` |
| “delete order O-123 confirm:true reason:'duplicate entry'” | Confirmed, soft delete | `delete_order` |

---

## ⚙️ Getting Started

### Prerequisites

- JDK 21+
- Maven 3.9+
- OpenAI API key *(or Azure OpenAI, HuggingFace, or local LLM endpoint)*
- Optional: Docker (if containerizing)

### Setup

```bash
# 1️⃣ Clone the repo
git clone https://github.com/<your-org>/trais-ai-agent.git
cd trais-ai-agent

# 2️⃣ Configure OpenAI credentials
export SPRING_AI_OPENAI_API_KEY=sk-xxxxxx
# or set in application.yml:
# spring.ai.openai.api-key: sk-xxxxxx

# 3️⃣ Run the app
mvn spring-boot:run
```

App runs by default on `http://localhost:8080`.

---

## 🧪 Try It Out

### Endpoint
```
POST /api/agent/chat
Content-Type: application/json
```

### Example request
```json
{
  "sessionId": "S-123",
  "message": "Find me all the orders for my dearest Suprada."
}
```

### Example response
```json
{
  "reply": "Here are all the orders for your dearest Suprada:\n1. O-b3b82692... Status: SHIPPED\n2. O-1b66e091... Status: SHIPPED"
}
```

### Delete flow
1. **User:**  `delete order O-123`
2. **Agent:**  “Please confirm: `delete order O-123 confirm:true reason:'duplicate entry'`”
3. **User:**  `delete order O-123 confirm:true reason:'duplicate entry'`
4. **Agent:**  “Order O-123 deleted successfully.”

---

## 🧩 Adding New Tools

1. Create a new method in `ToolConfig`.
2. Annotate with `@Tool(name = "tool_name", description = "...")`.
3. Accept a POJO parameter (record) for structured input.
4. Return a record as structured output.

Example:
```java
@Tool(name = "get_customer_balance", description = "Fetch balance for a customer.")
public BalanceDTO getCustomerBalance(CustomerRequest req) {
    return new BalanceDTO(req.customerId(), billingService.getBalance(req.customerId()));
}
```

The LLM will automatically learn to invoke it when user input implies that intent.

---

## 🧰 Environment Variables

| Variable | Description | Default |
|-----------|-------------|----------|
| `SPRING_AI_OPENAI_API_KEY` | OpenAI API key | — |
| `SPRING_PROFILES_ACTIVE` | Profile (`dev` / `prod`) | `dev` |
| `SERVER_PORT` | Server port | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC connection | `jdbc:h2:mem:orders` |

---

## 🧼 Safety & Audit Features

- 🧾 **Soft delete only** (`OrderStatus.DELETED`)
- 🧠 **LLM confirmation** before destructive action
- 🧍 **Actor & reason tracking**
- 🔄 **Idempotent deletes** (repeated requests don’t error)
- 💾 **Optional audit logging hook**

---

## 🧮 Example Database (H2 Console)

When running locally:
```
http://localhost:8080/h2-console
```
Use:
```
JDBC URL: jdbc:h2:mem:orders
User: sa
Password:
```

---

## 📦 Docker Build (optional)

```bash
docker build -t trais-ai-agent .
docker run -p 8080:8080 -e SPRING_AI_OPENAI_API_KEY=sk-xxxx trais-ai-agent
```

---

## 🔗 Extending Further

| Area | Ideas |
|------|-------|
| **LLM Hosting** | Swap to Azure OpenAI (`spring.ai.azure.openai`) or local Llama3 |
| **Auth / IAM** | Integrate JWT-based roles (to control who can delete orders) |
| **Memory** | Persist chat memory using Redis or SQL |
| **UI** | Add a React chat UI (already partially implemented in `/frontend`) |
| **Observability** | Add OpenTelemetry tracing for agent/tool calls |

---

## 🧩 Key Learning Goals

- How Spring AI bridges LLMs and business logic
- How to register Java methods as callable tools
- How to build reasoning-capable APIs (agents)
- How to enforce business constraints safely with AI
- How to integrate confirmation & audit workflows

---

## 📚 References

- [Spring AI Project](https://spring.io/projects/spring-ai)
- [OpenAI API Docs](https://platform.openai.com/docs)
- [Azure OpenAI Service](https://learn.microsoft.com/azure/ai-services/openai/)
- [LangChain Concepts](https://python.langchain.com/)
- [MCP (Model Context Protocol)](https://modelcontextprotocol.io/)

---

### 💬 Author

**Tulsi Rai**  
Principal Engineer / Solution Architect  
20+ years of Java + Cloud experience | AWS Certified Architect  
🚀 Exploring Agentic AI for enterprise modernization

