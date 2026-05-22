import { httpClient } from "@/utils/httpClient";

const BASE_URL = "/api/ai";

export interface ParamSpec {
  name: string;
  type: string;
  default: unknown;
  required: boolean;
  label: string;
}

export interface ChartHint {
  type: string;
  x: string;
  y: string;
  series?: string;
}

export interface GenerateSqlResult {
  sqlTemplate: string;
  paramsSpec: ParamSpec[];
  reason: string;
  chartHint: ChartHint | null;
  confidence: number;
  warnings: string[];
  /** ai_sql_memory.id，用于后续点赞/踩与执行关联 */
  memoryId?: number;
}

export interface ExecuteSqlResult {
  columns: string[];
  rows: unknown[][];
  chartUrl: string | null;
  sqlTemplate: string;
  params: Record<string, unknown>;
}

export interface HistoryMessage {
  role: "user" | "assistant";
  content: string;
}

// ── 对话管理相关接口 ──

export interface AiConversationResponse {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
  messages?: AiMessageResponse[];
}

export interface AiMessageResponse {
  id: number;
  role: "user" | "assistant";
  content: string;
  sqlResult?: GenerateSqlResult;
  errorMsg?: string;
  createdAt: string;
  /** 当前用户对该消息对应 memory 的反馈：+1 / -1 / null */
  feedbackVote?: number | null;
}

export const aiApi = {
  generateSql: (question: string, history?: HistoryMessage[]) =>
    httpClient<GenerateSqlResult>(`${BASE_URL}/generate-sql`, {
      method: "POST",
      body: { question, history: history?.length ? history : undefined },
    }),

  executeSql: (payload: {
    sqlTemplate: string;
    params: Record<string, unknown>;
    chartHint: ChartHint | null;
    question: string;
    memoryId?: number;
  }) =>
    httpClient<ExecuteSqlResult>(`${BASE_URL}/execute-sql`, {
      method: "POST",
      body: payload,
    }),

  feedback: (memoryId: number, vote: 1 | -1) =>
    httpClient<void>(`${BASE_URL}/feedback`, {
      method: "POST",
      body: { memoryId, vote },
    }),

  // ── 对话管理 API ──

  createConversation: (title?: string) =>
    httpClient<AiConversationResponse>(`${BASE_URL}/conversations`, {
      method: "POST",
      body: title ? { title } : {},
    }),

  getConversations: () =>
    httpClient<AiConversationResponse[]>(`${BASE_URL}/conversations`, {
      method: "GET",
    }),

  getConversationDetail: (id: number) =>
    httpClient<AiConversationResponse>(`${BASE_URL}/conversations/${id}`, {
      method: "GET",
    }),

  updateConversationTitle: (id: number, title: string) =>
    httpClient<void>(`${BASE_URL}/conversations/${id}`, {
      method: "PUT",
      body: { title },
    }),

  deleteConversation: (id: number) =>
    httpClient<void>(`${BASE_URL}/conversations/${id}`, {
      method: "DELETE",
    }),

  saveMessage: (conversationId: number, message: {
    role: "user" | "assistant";
    content: string;
    sqlResult?: GenerateSqlResult;
    errorMsg?: string;
  }) =>
    httpClient<AiMessageResponse>(`${BASE_URL}/conversations/${conversationId}/messages`, {
      method: "POST",
      body: {
        role: message.role,
        content: message.content,
        sqlResult: message.sqlResult ? JSON.stringify(message.sqlResult) : undefined,
        errorMsg: message.errorMsg,
      },
    }),
};
