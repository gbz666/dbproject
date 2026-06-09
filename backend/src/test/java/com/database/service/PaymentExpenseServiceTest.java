package com.database.service;

import com.database.dto.PaymentExpenseDTO;
import com.database.dto.PaymentExpenseQuery;
import com.database.mapper.PaymentExpensesMapper;
import com.database.pojo.PaymentExpenses;
import com.database.vo.PaymentExpenseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentExpenseService 单元测试。
 * 用 mock mapper 测 service 内部行为，不依赖数据库。
 */
class PaymentExpenseServiceTest {

    private PaymentExpensesMapper mapper;
    private PaymentExpenseService service;

    @BeforeEach
    void setUp() {
        mapper = mock(PaymentExpensesMapper.class);
        service = new PaymentExpenseService();
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    // ===== create =====

    @Test
    void create_normalInput_callsInsertSelectiveWithCorrectFields() {
        PaymentExpenseDTO dto = buildDTO();
        Long staffId = 99L;

        service.create(dto, staffId);

        ArgumentCaptor<PaymentExpenses> captor = ArgumentCaptor.forClass(PaymentExpenses.class);
        verify(mapper, times(1)).insertSelective(captor.capture());

        PaymentExpenses captured = captor.getValue();
        assertNull(captured.getId(), "新建时 id 应为 null");
        assertEquals("PAY-2026-001", captured.getPaymentNo());
        assertEquals(10L, captured.getSupplierId());
        assertEquals(20L, captured.getPurchaseInvoiceId());
        assertEquals(new BigDecimal("1500.00"), captured.getAmount());
        assertNotNull(captured.getPaymentDate(), "paymentDate 不应为 null");
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 6, 1)), captured.getPaymentDate());
        assertEquals("bank", captured.getMethod());
        assertEquals("测试备注", captured.getRemark());
        assertEquals(staffId, captured.getCreatedById(), "createdById 应等于 staffId");
        assertEquals(staffId, captured.getUpdatedById(), "updatedById 应等于 staffId");
    }

    @Test
    void create_paymentDateNull_doesNotThrow() {
        PaymentExpenseDTO dto = buildDTO();
        dto.setPaymentDate(null);
        Long staffId = 1L;

        assertDoesNotThrow(() -> service.create(dto, staffId));

        ArgumentCaptor<PaymentExpenses> captor = ArgumentCaptor.forClass(PaymentExpenses.class);
        verify(mapper).insertSelective(captor.capture());
        assertNull(captor.getValue().getPaymentDate(), "paymentDate 为 null 时应保持 null");
    }

    // ===== update =====

    @Test
    void update_normalInput_callsUpdateByPrimaryKeySelectiveWithIdSet() {
        PaymentExpenseDTO dto = buildDTO();
        Long id = 42L;
        Long staffId = 77L;

        service.update(id, dto, staffId);

        ArgumentCaptor<PaymentExpenses> captor = ArgumentCaptor.forClass(PaymentExpenses.class);
        verify(mapper, times(1)).updateByPrimaryKeySelective(captor.capture());

        PaymentExpenses captured = captor.getValue();
        assertEquals(id, captured.getId(), "更新时应设置 id");
        assertEquals("PAY-2026-001", captured.getPaymentNo());
        assertEquals(10L, captured.getSupplierId());
        assertEquals(20L, captured.getPurchaseInvoiceId());
        assertEquals(new BigDecimal("1500.00"), captured.getAmount());
        assertEquals("bank", captured.getMethod());
        assertEquals("测试备注", captured.getRemark());
        assertEquals(staffId, captured.getUpdatedById(), "updatedById 应等于 staffId");
        // 更新时不应设置 createdById
        assertNull(captured.getCreatedById(), "更新时 createdById 应为 null");
    }

    // ===== delete =====

    @Test
    void delete_callsDeleteByPrimaryKey() {
        Long id = 55L;

        service.delete(id);

        verify(mapper, times(1)).deleteByPrimaryKey(id);
    }

    // ===== findPage =====

    @Test
    void findPage_callsSelectPageListWithQuery() {
        PaymentExpenseQuery query = new PaymentExpenseQuery();
        query.setPageNum(2);
        query.setPageSize(15);
        query.setPaymentNo("PAY-001");

        PaymentExpenseVO vo = new PaymentExpenseVO();
        vo.setId(1L);
        vo.setPaymentNo("PAY-001");
        when(mapper.selectPageList(any(PaymentExpenseQuery.class))).thenReturn(List.of(vo));

        // PageHelper.startPage 是静态方法，在纯单元测试中不会真正启用分页拦截，
        // 但 service 方法调用不应抛异常，且 mapper 应被正确调用
        var result = service.findPage(query);

        verify(mapper, times(1)).selectPageList(query);
        assertNotNull(result, "返回的 PageInfo 不应为 null");
        assertFalse(result.getList().isEmpty(), "结果列表不应为空");
        assertEquals("PAY-001", result.getList().get(0).getPaymentNo());
    }

    // ===== helpers =====

    private static PaymentExpenseDTO buildDTO() {
        PaymentExpenseDTO dto = new PaymentExpenseDTO();
        dto.setPaymentNo("PAY-2026-001");
        dto.setSupplierId(10L);
        dto.setPurchaseInvoiceId(20L);
        dto.setAmount(new BigDecimal("1500.00"));
        dto.setPaymentDate(LocalDate.of(2026, 6, 1));
        dto.setMethod("bank");
        dto.setRemark("测试备注");
        return dto;
    }
}
