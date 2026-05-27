package com.database.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AI 问题归一化器
 *
 * 把任意中文自然语言查询转成"归一化字符串"，写入 ai_sql_memory.normalized_question，
 * 供 RAG 检索时做相似度匹配。
 *
 * 不做分词（百千条数据规模不需要），靠"去噪 + 同义词折叠 + 数字/时间占位符"
 * 把同义问题映射到接近的字符串。
 *
 * 例：
 *   "查询本季度销售额前 10 的产品"  → "__TIME__销售额前__NUM__产品"
 *   "看一下本季度营业额 top10 商品" → "__TIME__销售额top__NUM__产品"
 */
public final class AiSqlNormalizer {

    private AiSqlNormalizer() {}

    /** 同义词 → 统一词（LinkedHashMap 保序遍历，整词替换防误伤）*/
    private static final Map<String, String> SYNONYMS = new LinkedHashMap<>();
    static {
        SYNONYMS.put("营业额", "销售额");
        SYNONYMS.put("商品", "产品");
        SYNONYMS.put("盈利", "利润");
        SYNONYMS.put("顾客", "客户");
        SYNONYMS.put("比例", "占比");
        SYNONYMS.put("总额", "总计");
        SYNONYMS.put("合计", "总计");
        SYNONYMS.put("变化", "趋势");
    }

    /** 停用词（已按字符长度降序排，避免"查询"被"查"吃掉）*/
    private static final String[] STOPWORDS_DESC = {
            "查询", "统计", "帮我", "一下", "希望", "想要", "看一下",
            "多少", "哪些", "分布", "总共", "全部", "所有",
            "请", "看", "要", "想", "各", "每", "按", "最", "了",
            "的", "和", "是", "有", "在", "把", "对", "给", "与", "或", "等",
            "查"
    };

    /** 时间表达式：相对时间 + 绝对日期 */
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "今年|本年|本季度|本月|本周|去年|上月|上周|昨天|前天|今天" +
                    "|最近\\s*\\d*\\s*[天月周年]" +
                    "|\\d{4}年(?:\\d{1,2}月)?(?:\\d{1,2}[日号])?" +
                    "|\\d{1,2}月\\d{1,2}[日号]?"
    );

    /** 中文数字 */
    private static final Pattern CN_NUM_PATTERN = Pattern.compile("[一二三四五六七八九十百千万零]+");

    /** 阿拉伯数字 */
    private static final Pattern AR_NUM_PATTERN = Pattern.compile("\\d+");

    /** 保留字符：中文 + 字母 + 数字 + 下划线（占位符用）+ 百分号 */
    private static final Pattern KEEP_CHAR = Pattern.compile("[\\u4e00-\\u9fa5a-zA-Z0-9_%]");

    public static String normalize(String question) {
        if (question == null || question.isBlank()) return "";

        String s = question.toLowerCase();

        // 1) 同义词折叠
        for (Map.Entry<String, String> e : SYNONYMS.entrySet()) {
            s = s.replace(e.getKey(), e.getValue());
        }

        // 2) 时间词 → __TIME__
        s = TIME_PATTERN.matcher(s).replaceAll("__TIME__");

        // 3) 数字 → __NUM__（先中文再阿拉伯，二者不会冲突）
        s = CN_NUM_PATTERN.matcher(s).replaceAll("__NUM__");
        s = AR_NUM_PATTERN.matcher(s).replaceAll("__NUM__");

        // 4) 去标点 / 去空白：只保留中文 + 字母 + 数字 + 下划线 + 百分号
        StringBuilder cleaned = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (KEEP_CHAR.matcher(String.valueOf(c)).matches()) {
                cleaned.append(c);
            }
        }
        s = cleaned.toString();

        // 5) 去停用词（长词优先）
        for (String stop : STOPWORDS_DESC) {
            if (s.contains(stop)) s = s.replace(stop, "");
        }

        return s;
    }
}
