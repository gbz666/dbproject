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
  }) =>
    httpClient<ExecuteSqlResult>(`${BASE_URL}/execute-sql`, {
      method: "POST",
      body: payload,
    }),
};
