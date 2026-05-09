import { aiApi } from "@/api/aiApi";
import type {
  GenerateSqlResult,
  ExecuteSqlResult,
  ChartHint,
  HistoryMessage,
  AiConversationResponse,
  AiMessageResponse,
} from "@/api/aiApi";

export const aiService = {
  async generateSql(
    question: string,
    history?: HistoryMessage[]
  ): Promise<GenerateSqlResult> {
    return await aiApi.generateSql(question, history);
  },

  async executeSql(
    sqlTemplate: string,
    params: Record<string, unknown>,
    chartHint: ChartHint | null,
    question: string
  ): Promise<ExecuteSqlResult> {
    return await aiApi.executeSql({ sqlTemplate, params, chartHint, question });
  },

  // ── 对话管理 ──

  async createConversation(title?: string): Promise<AiConversationResponse> {
    return await aiApi.createConversation(title);
  },

  async getConversations(): Promise<AiConversationResponse[]> {
    return await aiApi.getConversations();
  },

  async getConversationDetail(id: number): Promise<AiConversationResponse> {
    return await aiApi.getConversationDetail(id);
  },

  async updateConversationTitle(id: number, title: string): Promise<void> {
    return await aiApi.updateConversationTitle(id, title);
  },

  async deleteConversation(id: number): Promise<void> {
    return await aiApi.deleteConversation(id);
  },

  async saveMessage(
    conversationId: number,
    message: {
      role: "user" | "assistant";
      content: string;
      sqlResult?: GenerateSqlResult;
      errorMsg?: string;
    }
  ): Promise<AiMessageResponse> {
    return await aiApi.saveMessage(conversationId, message);
  },
};
