package com.database.service;

/**
 * 给归一化后的 question 打意图标签，写入 ai_sql_memory.intent_tag。
 *
 * RAG 检索时同 intent_tag 的 memory 加权排前面（同类问题往往生成同类 SQL）。
 *
 * 标签集合：ranking / trend / proportion / summary / detail
 */
public final class AiIntentTagger {

    private AiIntentTagger() {}

    public enum Tag {
        RANKING("ranking"),
        TREND("trend"),
        PROPORTION("proportion"),
        SUMMARY("summary"),
        DETAIL("detail");

        private final String code;
        Tag(String code) { this.code = code; }
        public String code() { return code; }
    }

    /**
     * @param normalizedQuestion 归一化后的问题（已 lower-case，包含 __NUM__/__TIME__ 占位符）
     */
    public static String tag(String normalizedQuestion) {
        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return Tag.DETAIL.code();
        }
        String s = normalizedQuestion;

        // ranking：前 N / top N / 排行 / 排名
        if (s.contains("前__num__") || s.contains("top__num__") || s.contains("排行") || s.contains("排名")) {
            return Tag.RANKING.code();
        }
        // trend：趋势 / 各__TIME__ / 逐月逐年
        if (s.contains("趋势") || s.contains("各__time__") || s.contains("逐月") || s.contains("逐年")) {
            return Tag.TREND.code();
        }
        // proportion：占比
        if (s.contains("占比")) {
            return Tag.PROPORTION.code();
        }
        // summary：总计 / 总额（已被归一化器统一成"总计"）
        if (s.contains("总计") || s.contains("总和") || s.contains("汇总")) {
            return Tag.SUMMARY.code();
        }
        return Tag.DETAIL.code();
    }
}
