import { defineStore } from "pinia";
import { ref, computed, reactive } from "vue";
import { aiService } from "@/services/aiService";
import type {
  GenerateSqlResult,
  ExecuteSqlResult,
  HistoryMessage,
  ChartHint,
  ParamSpec,
} from "@/api/aiApi";

export interface ChatItem {
  id: string;
  role: "user" | "assistant";
  content: string;
  sqlResult?: GenerateSqlResult;
  loading?: boolean;
  error?: string;
  /** 后端消息 ID（已持久化的消息才有） */
  dbId?: number;
  /** 本地反馈状态：1=已点赞，-1=已点踩，undefined=未操作。
   *  本期只在当前会话内追踪，刷新后丢失（下一版从后端读取） */
  feedbackVote?: 1 | -1;
}

export interface Conversation {
  /** 后端对话 ID（数字） */
  id: number;
  title: string;
  createdAt: number;
  updatedAt: number;
  messages: ChatItem[];
}

let _seq = 0;
function uid(): string {
  return `m-${Date.now()}-${++_seq}`;
}

/** 将 SQL 模板中的 {param} 占位符替换为参数值，生成可直接执行的 SQL */
function renderDefaults(
  sqlTemplate: string,
  paramsSpec: ParamSpec[],
  params?: Record<string, unknown>,
): string {
  let sql = sqlTemplate;
  for (const p of paramsSpec ?? []) {
    const val = params?.[p.name] ?? p.default;
    if (val != null) {
      // 根据参数类型决定是否加引号：数值类型不加引号，其他类型加引号
      const isNumericType =
        p.type === "int" || p.type === "float" || p.type === "number";
      const strVal = isNumericType ? String(val) : `'${val}'`;
      sql = sql.split(`{${p.name}}`).join(strVal);
    }
  }
  return sql;
}

/** 时间戳转换：后端返回 ISO 字符串，转为毫秒时间戳 */
function toTimestamp(dateStr: string | Date | undefined): number {
  if (!dateStr) return Date.now();
  return new Date(dateStr).getTime();
}

export const useAiStore = defineStore("ai", () => {
  // ── 多对话管理（单一数据源） ──
  const conversations = ref<Conversation[]>([]);
  const currentConversationId = ref<number | null>(null);
  const loadingConversations = ref(false);

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
  const panelSql = ref(""); // 原始 SQL 模板（含 {param} 占位符）
  const panelParamsSpec = ref<ParamSpec[]>([]);
  const panelParams = ref<Record<string, unknown>>({});
  const panelChartHint = ref<ChartHint | null>(null);
  const panelQuestion = ref("");
  const panelMemoryId = ref<number | undefined>(undefined);
  const executing = ref(false);
  const execResult = ref<ExecuteSqlResult | null>(null);
  const execError = ref("");
  const panelActive = computed(() => panelSql.value.length > 0);

  /** 根据模板 + 参数动态生成可执行 SQL */
  const renderedSql = computed(() =>
    renderDefaults(panelSql.value, panelParamsSpec.value, panelParams.value),
  );

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

  // ── 从后端加载对话列表 ──

  async function loadConversations(): Promise<boolean> {
    loadingConversations.value = true;
    try {
      const list = await aiService.getConversations();
      conversations.value = list.map((c) => ({
        id: c.id,
        title: c.title,
        createdAt: toTimestamp(c.createdAt),
        updatedAt: toTimestamp(c.updatedAt),
        messages: [], // 消息延迟加载
      }));

      if (conversations.value.length > 0 && currentConversationId.value === null) {
        currentConversationId.value = conversations.value[0].id;
        await loadMessages(currentConversationId.value);
      }
      return true;
    } catch (e) {
      console.warn("加载对话列表失败:", e);
      return false;
    } finally {
      loadingConversations.value = false;
    }
  }

  /** 加载指定对话的消息 */
  async function loadMessages(conversationId: number) {
    const conv = conversations.value.find((c) => c.id === conversationId);
    if (!conv) return;

    try {
      const detail = await aiService.getConversationDetail(conversationId);
      conv.messages = (detail.messages ?? []).map((m) => ({
        id: uid(),
        dbId: m.id,
        role: m.role,
        content: m.content,
        sqlResult: m.sqlResult,
        error: m.errorMsg,
        loading: false,
      }));
    } catch (e) {
      console.warn("加载消息失败:", e);
    }
  }

  // ── 对话管理 ──

  async function createConversation(): Promise<number> {
    try {
      const result = await aiService.createConversation();
      const conv: Conversation = {
        id: result.id,
        title: result.title,
        createdAt: toTimestamp(result.createdAt),
        updatedAt: toTimestamp(result.updatedAt),
        messages: [],
      };
      conversations.value.unshift(conv);
      currentConversationId.value = conv.id;
      inputText.value = "";
      clearPanel();
      return conv.id;
    } catch (e) {
      console.error("创建对话失败:", e);
      throw e;
    }
  }

  async function switchConversation(id: number) {
    if (id === currentConversationId.value) return;
    currentConversationId.value = id;
    inputText.value = "";
    sending.value = false;
    clearPanel();

    // 如果消息未加载，则加载
    const conv = conversations.value.find((c) => c.id === id);
    if (conv && conv.messages.length === 0) {
      await loadMessages(id);
    }
  }

  async function deleteConversation(id: number) {
    try {
      await aiService.deleteConversation(id);

      const idx = conversations.value.findIndex((c) => c.id === id);
      if (idx === -1) return;
      conversations.value.splice(idx, 1);

      if (id === currentConversationId.value) {
        if (conversations.value.length > 0) {
          currentConversationId.value = conversations.value[0].id;
          await loadMessages(currentConversationId.value);
        } else {
          await createConversation();
        }
      }
    } catch (e) {
      console.error("删除对话失败:", e);
      throw e;
    }
  }

  // ── 对话逻辑 ──

  function buildHistory(): HistoryMessage[] {
    const hist: HistoryMessage[] = [];
    for (const msg of messages.value) {
      if (msg.role === "user") {
        hist.push({ role: "user", content: msg.content });
      } else if (msg.role === "assistant" && msg.sqlResult) {
        // 将 assistant 的 JSON 结果转换为自然语言摘要，避免 LLM 困惑
        const reason = msg.sqlResult.reason || "已生成 SQL 查询";
        hist.push({
          role: "assistant",
          content: reason,
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
      await createConversation();
    }

    const conv = currentConversation.value!;

    // 1. 添加用户消息到本地
    const userMsg: ChatItem = { id: uid(), role: "user", content: text };
    conv.messages.push(userMsg);
    inputText.value = "";

    // 自动更新标题
    if (conv.title === "新对话") {
      const newTitle = text.length > 30 ? text.slice(0, 30) + "..." : text;
      conv.title = newTitle;
      // 异步更新后端标题（不阻塞）
      aiService.updateConversationTitle(conv.id, newTitle).catch((e) => {
        console.warn("更新对话标题失败:", e);
      });
    }

    // 2. 保存用户消息到后端（异步，不阻塞）
    aiService
      .saveMessage(conv.id, { role: "user", content: text })
      .then((saved) => {
        userMsg.dbId = saved.id;
      })
      .catch((e) => {
        console.warn("保存用户消息失败:", e);
      });

    // 3. 用 reactive() 包裹，确保后续属性赋值通过 Vue Proxy 追踪
    const assistantMsg = reactive<ChatItem>({
      id: uid(),
      role: "assistant",
      content: "",
      loading: true,
      sqlResult: undefined,
    });

    // 4. 将引用推入源数组
    conv.messages.push(assistantMsg);
    sending.value = true;

    try {
      const history = buildHistory().slice(0, -1);
      const result = await aiService.generateSql(
        text,
        history.length ? history : undefined,
      );

      // 5. 直接修改局部引用的对象，Vue 会自动侦测到深层变更
      if (result) {
        assistantMsg.sqlResult = result;
        assistantMsg.content = result.reason || "SQL 已生成";

        // 保存 AI 消息到后端
        aiService
          .saveMessage(conv.id, {
            role: "assistant",
            content: assistantMsg.content,
            sqlResult: result,
          })
          .then((saved) => {
            assistantMsg.dbId = saved.id;
          })
          .catch((e) => {
            console.warn("保存 AI 消息失败:", e);
          });
      } else {
        assistantMsg.error = "AI 服务返回数据为空";
        assistantMsg.content = assistantMsg.error;
      }
    } catch (err: any) {
      console.error("[sendMessage] generateSql 失败:", err);
      assistantMsg.error = err?.message || "SQL 生成失败，请重试";
      assistantMsg.content = assistantMsg.error ?? "未知错误";

      // 保存错误消息到后端
      aiService
        .saveMessage(conv.id, {
          role: "assistant",
          content: assistantMsg.content,
          errorMsg: assistantMsg.error,
        })
        .catch((e) => {
          console.warn("保存错误消息失败:", e);
        });
    } finally {
      assistantMsg.loading = false;
      sending.value = false;
      conv.updatedAt = Date.now();
    }
  }

  // ── 面板逻辑 ──

  function loadToPanel(result: GenerateSqlResult, question: string) {
    panelSql.value = result.sqlTemplate; // 保存原始模板
    panelParamsSpec.value = result.paramsSpec ?? [];
    // 初始化参数默认值
    const initParams: Record<string, unknown> = {};
    for (const p of result.paramsSpec ?? []) {
      if (p.default != null) initParams[p.name] = p.default;
    }
    panelParams.value = initParams;
    panelChartHint.value = result.chartHint;
    panelQuestion.value = question;
    panelMemoryId.value = result.memoryId;
    execResult.value = null;
    execError.value = "";
  }

  async function executePanel() {
    const sql = renderedSql.value.trim();
    if (!sql) return;
    executing.value = true;
    execResult.value = null;
    execError.value = "";
    try {
      execResult.value = await aiService.executeSql(
        sql,
        panelParams.value,
        panelChartHint.value,
        panelQuestion.value,
        panelMemoryId.value,
      );
    } catch (err: any) {
      execError.value = err?.message || "SQL 执行失败";
    } finally {
      executing.value = false;
    }
  }

  async function submitFeedback(msg: ChatItem, vote: 1 | -1): Promise<void> {
    const memoryId = msg.sqlResult?.memoryId;
    if (!memoryId) {
      console.warn("submitFeedback: 当前消息没有 memoryId，无法提交反馈");
      return;
    }
    // 再点同一票视为取消（前端体验优化），但后端只支持 +1/-1，不撤销，
    // 所以仅本地视觉切换为未选中状态，不再 POST
    if (msg.feedbackVote === vote) {
      msg.feedbackVote = undefined;
      return;
    }
    try {
      await aiService.feedback(memoryId, vote);
      msg.feedbackVote = vote;
    } catch (e) {
      console.error("提交反馈失败:", e);
      throw e;
    }
  }

  function clearConversation() {
    messages.value.splice(0);
    inputText.value = "";
    sending.value = false;
  }

  function clearPanel() {
    panelSql.value = "";
    panelParamsSpec.value = [];
    panelParams.value = {};
    panelChartHint.value = null;
    panelMemoryId.value = undefined;
    execResult.value = null;
    execError.value = "";
  }

  // ── 初始化 ──
  // 从后端加载对话列表（异步）
  loadConversations().then((loaded) => {
    if (!loaded || conversations.value.length === 0) {
      // 如果加载失败或没有对话，创建一个新对话
      createConversation().catch((e) => {
        console.error("初始化创建对话失败:", e);
      });
    }
  });

  return {
    // 多对话管理
    conversations,
    currentConversationId,
    currentConversation,
    loadingConversations,
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
    panelParamsSpec,
    panelParams,
    renderedSql,
    panelChartHint,
    panelQuestion,
    panelMemoryId,
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
    submitFeedback,
    clearConversation,
    clearPanel,
  };
});
