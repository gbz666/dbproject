package com.database.service;

import com.database.mapper.AiSqlExecLogMapper;
import com.database.mapper.AiSqlFeedbackMapper;
import com.database.mapper.AiSqlMemoryMapper;
import com.database.pojo.AiSqlMemory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiMemoryService 检索/失败降级逻辑的单测。
 * 用 mock mapper 测 service 内部行为，不依赖数据库。
 */
class AiMemoryServiceRetrieveTest {

    private AiSqlMemoryMapper memoryMapper;
    private AiSqlFeedbackMapper feedbackMapper;
    private AiSqlExecLogMapper execLogMapper;
    private AiMemoryService service;

    @BeforeEach
    void setUp() {
        memoryMapper = mock(AiSqlMemoryMapper.class);
        feedbackMapper = mock(AiSqlFeedbackMapper.class);
        execLogMapper = mock(AiSqlExecLogMapper.class);
        service = new AiMemoryService(memoryMapper, feedbackMapper, execLogMapper, new ObjectMapper());
    }

    // ===== retrieveCandidates =====

    @Test
    void retrieve_blankQuestion_returnsEmpty() {
        List<AiSqlMemory> r = service.retrieveCandidates("", 3);
        assertTrue(r.isEmpty());
        verifyNoInteractions(memoryMapper);
    }

    @Test
    void retrieve_topKZero_returnsEmptyWithoutDbCall() {
        List<AiSqlMemory> r = service.retrieveCandidates("销售前10的产品", 0);
        assertTrue(r.isEmpty());
        verifyNoInteractions(memoryMapper);
    }

    @Test
    void retrieve_passesGramsAndIntentToMapper() {
        when(memoryMapper.searchCandidates(anyList(), anyString(), any(), anyInt()))
                .thenReturn(List.of());

        service.retrieveCandidates("查询本季度销售额前 10 的产品", 3);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> gramsCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> intentCap = ArgumentCaptor.forClass(String.class);
        verify(memoryMapper).searchCandidates(gramsCap.capture(), intentCap.capture(), any(), anyInt());

        // 归一化后是 __time__销售额前__num__产品（去停用词后）
        // 切 2-gram 时跳过占位符，剩 "销售额产品" 这种短串
        assertFalse(gramsCap.getValue().isEmpty(), "应至少切出 1 个 gram");
        assertEquals("ranking", intentCap.getValue(), "前N 应识别为 ranking");
    }

    @Test
    void retrieve_truncatesToTopK() {
        // mapper 返回 6 条，retrieveCandidates(_, 2) 应只取前 2
        List<AiSqlMemory> raw = List.of(
                buildMemory(1L), buildMemory(2L), buildMemory(3L),
                buildMemory(4L), buildMemory(5L), buildMemory(6L)
        );
        when(memoryMapper.searchCandidates(anyList(), anyString(), any(), anyInt()))
                .thenReturn(raw);

        List<AiSqlMemory> r = service.retrieveCandidates("销售产品", 2);
        assertEquals(2, r.size());
        assertEquals(1L, r.get(0).getId());
        assertEquals(2L, r.get(1).getId());
    }

    @Test
    void retrieve_passesQualityGateParams() {
        when(memoryMapper.searchCandidates(anyList(), anyString(), any(), anyInt()))
                .thenReturn(List.of());

        service.retrieveCandidates("销售产品", 3);

        ArgumentCaptor<BigDecimal> minConfCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(memoryMapper).searchCandidates(anyList(), anyString(), minConfCap.capture(), anyInt());
        // 检索质量门槛 confidence >= 0.30
        assertEquals(0, minConfCap.getValue().compareTo(new BigDecimal("0.30")));
    }

    // ===== recordExecFailure =====

    @Test
    void recordFailure_nullMemoryId_noop() {
        service.recordExecFailure(null);
        verifyNoInteractions(execLogMapper, memoryMapper);
    }

    @Test
    void recordFailure_singleFailure_doesNotDemote() {
        when(execLogMapper.countFailuresByMemoryId(42L)).thenReturn(1);

        service.recordExecFailure(42L);

        verify(execLogMapper).countFailuresByMemoryId(42L);
        verify(memoryMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void recordFailure_thresholdReached_demotesToFailed() {
        when(execLogMapper.countFailuresByMemoryId(42L)).thenReturn(2);
        AiSqlMemory existing = buildMemory(42L);
        existing.setStatus("draft");
        when(memoryMapper.selectById(42L)).thenReturn(existing);

        service.recordExecFailure(42L);

        verify(memoryMapper).updateStatus(42L, "failed");
    }

    @Test
    void recordFailure_alreadyFailed_skipsUpdate() {
        when(execLogMapper.countFailuresByMemoryId(42L)).thenReturn(5);
        AiSqlMemory existing = buildMemory(42L);
        existing.setStatus("failed");
        when(memoryMapper.selectById(42L)).thenReturn(existing);

        service.recordExecFailure(42L);

        verify(memoryMapper, never()).updateStatus(anyLong(), anyString());
    }

    // ===== helpers =====

    private static AiSqlMemory buildMemory(long id) {
        AiSqlMemory m = new AiSqlMemory();
        m.setId(id);
        m.setStatus("draft");
        m.setSuccessCount(1);
        m.setConfidence(new BigDecimal("0.5"));
        return m;
    }
}
