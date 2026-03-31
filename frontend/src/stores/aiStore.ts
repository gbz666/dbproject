import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { aiService } from "@/services/aiService";
import type {
  GenerateSqlResult,
  ExecuteSqlResult,
  HistoryMessage,
  ChartHint,
} from "@/api/aiApi";

export interface ChatItem {
  id: string;
  role: "user" | "assistant";
  content: string;
  sqlResult?: GenerateSqlResult;
  loading?: boolean;
  error?: string;
}

let _seq = 0;
function uid(): string {
  return `m-${Date.now()}-${++_seq}`;
}

/** 将 SQL 模板中的 {param} 占位符替换为默认值，生成可直接执行的 SQL */
function renderDefaults(sqlTemplate: string, paramsSpec: GenerateSqlResult["paramsSpec"]): string {
  let sql = sqlTemplate;
  for (const p of paramsSpec ?? []) {
    if (p.default != null) {
      const val = typeof p.default === "string" ? `'${p.default}'` : String(p.default);
      sql = sql.split(`{${p.name}}`).join(val);
    }
  }
  return sql;
}

export const useAiStore = defineStore("ai", () => {
  // ── 对话区 ──
  const messages = ref<ChatItem[]>([]);
  const inputText = ref("");
  const sending = ref(false);
  const hasInput = computed(() => inputText.value.trim().length > 0);

  // ── 执行面板 ──
  const panelSql = ref("");
  const panelChartHint = ref<ChartHint | null>(null);
  const panelQuestion = ref("");
  const executing = ref(false);
  const execResult = ref<ExecuteSqlResult | null>(null);
  const execError = ref("");
  const panelActive = computed(() => panelSql.value.length > 0);

  const chartFullUrl = computed(() => {
    if (!execResult.value?.chartUrl) return "";
    const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    return `${base}${execResult.value.chartUrl}`;
  });

  const tableRows = computed(() => {
    if (!execResult.value) return [];
    const { columns, rows } = execResult.value;
    return rows.map((row) => {
      const obj: Record<string, unknown> = {};
      columns.forEach((col, i) => { obj[col] = row[i]; });
      return obj;
    });
  });

  // ── 对话逻辑 ──

  function buildHistory(): HistoryMessage[] {
    const hist: HistoryMessage[] = [];
    for (const msg of messages.value) {
      if (msg.role === "user") {
        hist.push({ role: "user", content: msg.content });
      } else if (msg.role === "assistant" && msg.sqlResult) {
        hist.push({
          role: "assistant",
          content: JSON.stringify({
            sqlTemplate: msg.sqlResult.sqlTemplate,
            reason: msg.sqlResult.reason,
            chartHint: msg.sqlResult.chartHint,
          }),
        });
      }
    }
    return hist;
  }

  async function sendMessage() {
    const text = inputText.value.trim();
    if (!text) return;

    messages.value.push({ id: uid(), role: "user", content: text });
    inputText.value = "";

    const aMsg: ChatItem = { id: uid(), role: "assistant", content: "", loading: true };
    messages.value.push(aMsg);
    sending.value = true;

    try {
      const history = buildHistory().slice(0, -1);
      const result = await aiService.generateSql(text, history.length ? history : undefined);
      aMsg.sqlResult = result;
      aMsg.content = result.reason || "SQL 已生成";
    } catch (err: any) {
      aMsg.error = err?.message || "SQL 生成失败，请重试";
      aMsg.content = aMsg.error ?? "未知错误";
    } finally {
      aMsg.loading = false;
      sending.value = false;
    }
  }

  // ── 面板逻辑 ──

  function loadToPanel(result: GenerateSqlResult, question: string) {
    panelSql.value = renderDefaults(result.sqlTemplate, result.paramsSpec);
    panelChartHint.value = result.chartHint;
    panelQuestion.value = question;
    execResult.value = null;
    execError.value = "";
  }

  async function executePanel() {
    if (!panelSql.value.trim()) return;
    executing.value = true;
    execResult.value = null;
    execError.value = "";
    try {
      execResult.value = await aiService.executeSql(
        panelSql.value,
        {},
        panelChartHint.value,
        panelQuestion.value,
      );
    } catch (err: any) {
      execError.value = err?.message || "SQL 执行失败";
    } finally {
      executing.value = false;
    }
  }

  function clearConversation() {
    messages.value = [];
    inputText.value = "";
    sending.value = false;
  }

  function clearPanel() {
    panelSql.value = "";
    panelChartHint.value = null;
    execResult.value = null;
    execError.value = "";
  }

  return {
    messages, inputText, sending, hasInput,
    panelSql, panelChartHint, panelQuestion,
    executing, execResult, execError, panelActive,
    chartFullUrl, tableRows,
    sendMessage, loadToPanel, executePanel,
    clearConversation, clearPanel,
  };
});
