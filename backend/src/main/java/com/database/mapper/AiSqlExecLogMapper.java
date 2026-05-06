package com.database.mapper;

import com.database.pojo.AiSqlExecLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【ai_sql_exec_log(AI SQL 执行日志表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiSqlExecLog
 */
public interface AiSqlExecLogMapper {

    /**
     * 插入一条执行日志
     */
    void insert(AiSqlExecLog log);

    /**
     * 根据用户 ID 查询执行日志（按时间倒序）
     */
    List<AiSqlExecLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询最近 N 条执行日志
     */
    List<AiSqlExecLog> selectRecent(@Param("limit") int limit);

    /**
     * 根据 SQL 哈希查询（用于去重/统计）
     */
    List<AiSqlExecLog> selectBySqlHash(@Param("sqlHash") String sqlHash);
}
