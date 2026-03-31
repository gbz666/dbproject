import { aiApi } from "@/api/aiApi";
import type {
  GenerateSqlResult,
  ExecuteSqlResult,
  ChartHint,
  HistoryMessage,
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
};
