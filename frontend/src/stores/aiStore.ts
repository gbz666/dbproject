import { defineStore } from "pinia";
import { ref, computed, reactive } from "vue";
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

export interface Conversation {
  id: string;
  title: string;
  createdAt: number;
  updatedAt: number;
  messages: ChatItem[];
}

let _seq = 0;
function uid(): string {
  return `m-${Date.now()}-${++_seq}`;
}

function convId(): string {
  return `conv-${Date.now()}-${++_seq}`;
}

const STORAGE_KEY = "ai_conversations";

/** 将 SQL 模板中的 {param} 占位符替换为默认值，生成可直接执行的 SQL */
function renderDefaults(
  sqlTemplate: string,
  paramsSpec: GenerateSqlResult["paramsSpec"],
): string {
  let sql = sqlTemplate;
  for (const p of paramsSpec ?? []) {
    if (p.default != null) {
      const val =
        typeof p.default === "string" ? `'${p.default}'` : String(p.default);
      sql = sql.split(`{${p.name}}`).join(val);
    }
  }
  return sql;
}

export const useAiStore = defineStore("ai", () => {
  // ── 多对话管理（单一数据源） ──
  const conversations = ref<Conversation[]>([]);
  const currentConversationId = ref<string | null>(null);

  const currentConversation = computed(
    () =>
      conversations.value.find((c) => c.id === currentConversationId.value) ??
      null,
  );

  // messages 是 currentConversation.messages 的直接引用，无需手动同步
  const messages = computed<ChatItem[]>(
    () => currentConversation.value?.messages ?? [],
  );

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
      columns.forEach((col, i) => {
        obj[col] = row[i];
      });
      return obj;
    });
  });

  // ── 持久化 ──

  function persistConversations() {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          version: 1,
          conversations: conversations.value,
          currentConversationId: currentConversationId.value,
        }),
      );
    } catch (e) {
      console.warn("保存对话历史失败:", e);
    }
  }

  function loadConversations() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return false;
      const data = JSON.parse(raw);
      if (data.version === 1 && Array.isArray(data.conversations)) {
        // 恢复时重置所有 loading 状态（页面刷新后无活跃请求）
        for (const conv of data.conversations) {
          if (Array.isArray(conv.messages)) {
            for (const msg of conv.messages) {
              msg.loading = false;
            }
          }
        }
        conversations.value = data.conversations;
        currentConversationId.value = data.currentConversationId ?? null;
        return true;
      }
    } catch (e) {
      console.warn("读取对话历史失败:", e);
    }
    return false;
  }

  // ── 对话管理 ──

  function createConversation(): string {
    const id = convId();
    const now = Date.now();
    const conv: Conversation = {
      id,
      title: "新对话",
      createdAt: now,
      updatedAt: now,
      messages: [],
    };
    conversations.value.unshift(conv);
    currentConversationId.value = id;
    inputText.value = "";
    clearPanel();
    persistConversations();
    return id;
  }

  function switchConversation(id: string) {
    if (id === currentConversationId.value) return;
    currentConversationId.value = id;
    inputText.value = "";
    sending.value = false;
    clearPanel();
    persistConversations();
  }

  function deleteConversation(id: string) {
    const idx = conversations.value.findIndex((c) => c.id === id);
    if (idx === -1) return;
    conversations.value.splice(idx, 1);
    if (id === currentConversationId.value) {
      if (conversations.value.length > 0) {
        currentConversationId.value = conversations.value[0].id;
      } else {
        createConversation();
      }
    }
    persistConversations();
  }

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

    // 确保有当前对话
    if (!currentConversation.value) {
      createConversation();
    }

    const conv = currentConversation.value!;

    // 1. 直接操作 conv.messages
    conv.messages.push({ id: uid(), role: "user", content: text });
    inputText.value = "";

    if (conv.title === "新对话") {
      conv.title = text.length > 30 ? text.slice(0, 30) + "..." : text;
    }

    // 2. 用 reactive() 包裹，确保后续属性赋值通过 Vue Proxy 追踪
    const assistantMsg = reactive<ChatItem>({
      id: uid(),
      role: "assistant",
      content: "",
      loading: true,
      sqlResult: undefined,
    });

    // 3. 将引用推入源数组
    conv.messages.push(assistantMsg);
    sending.value = true;

    try {
      const history = buildHistory().slice(0, -1);
      const result = await aiService.generateSql(
        text,
        history.length ? history : undefined,
      );

      // 4. 直接修改局部引用的对象，Vue 会自动侦测到深层变更
      if (result) {
        assistantMsg.sqlResult = result;
        assistantMsg.content = result.reason || "SQL 已生成";
      } else {
        assistantMsg.error = "AI 服务返回数据为空";
        assistantMsg.content = assistantMsg.error;
      }
    } catch (err: any) {
      console.error("[sendMessage] generateSql 失败:", err);
      assistantMsg.error = err?.message || "SQL 生成失败，请重试";
      assistantMsg.content = assistantMsg.error ?? "未知错误";
    } finally {
      assistantMsg.loading = false;
      sending.value = false;
      conv.updatedAt = Date.now();
      try {
        persistConversations();
      } catch (e) {
        console.warn("[sendMessage] 持久化失败:", e);
      }
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
    messages.value.splice(0);
    inputText.value = "";
    sending.value = false;
    persistConversations();
  }

  function clearPanel() {
    panelSql.value = "";
    panelChartHint.value = null;
    execResult.value = null;
    execError.value = "";
  }

  // ── 初始化 ──
  const loaded = loadConversations();
  if (!loaded || conversations.value.length === 0) {
    const id = convId();
    const now = Date.now();
    conversations.value = [
      {
        id,
        title: "新对话",
        createdAt: now,
        updatedAt: now,
        messages: [],
      },
    ];
    currentConversationId.value = id;
  }

  return {
    // 多对话管理
    conversations,
    currentConversationId,
    currentConversation,
    createConversation,
    switchConversation,
    deleteConversation,
    // 对话区
    messages,
    inputText,
    sending,
    hasInput,
    // 执行面板
    panelSql,
    panelChartHint,
    panelQuestion,
    executing,
    execResult,
    execError,
    panelActive,
    chartFullUrl,
    tableRows,
    // 方法
    sendMessage,
    loadToPanel,
    executePanel,
    clearConversation,
    clearPanel,
  };
});
