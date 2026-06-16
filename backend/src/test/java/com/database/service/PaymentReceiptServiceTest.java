package com.database.service;

import com.database.dto.PaymentReceiptDTO;
import com.database.dto.PaymentReceiptQuery;
import com.database.mapper.PaymentReceiptsMapper;
import com.database.pojo.PaymentReceipts;
import com.database.vo.PaymentReceiptVO;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentReceiptService 纯单元测试。
 * 用 mock mapper 测 service 内部行为，不依赖数据库。
 */
class PaymentReceiptServiceTest {

    private PaymentReceiptsMapper mapper;
    private PaymentReceiptService service;

    @BeforeEach
    void setUp() throws Exception {
        mapper = mock(PaymentReceiptsMapper.class);
        service = new PaymentReceiptService();
        // PaymentReceiptService 使用 @Autowired 字段注入，通过反射设置 mock
        Field mapperField = PaymentReceiptService.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(service, mapper);
    }

    // ===== create =====

    @Test
    void create_normalInput_callsInsertSelectiveWithCorrectParams() {
        PaymentReceiptDTO dto = buildDTO();
        Long staffId = 100L;

        service.create(dto, staffId);

        ArgumentCaptor<PaymentReceipts> captor = ArgumentCaptor.forClass(PaymentReceipts.class);
        verify(mapper).insertSelective(captor.capture());

        PaymentReceipts captured = captor.getValue();
        assertNull(captured.getId(), "新建时 id 不应被设置");
        assertEquals("SK-20260601-001", captured.getReceiptNo());
        assertEquals(1L, captured.getCustomerId());
        assertEquals(10L, captured.getSalesInvoiceId());
        assertEquals(new BigDecimal("9999.99"), captured.getAmount());
        assertNotNull(captured.getReceiptDate(), "receiptDate 非空时应被转换");
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 6, 1)), captured.getReceiptDate());
        assertEquals("bank", captured.getMethod());
        assertEquals("测试备注", captured.getRemark());
        assertEquals(staffId, captured.getCreatedById(), "createdById 应为 staffId");
        assertEquals(staffId, captured.getUpdatedById(), "updatedById 应为 staffId");
    }

    @Test
    void create_receiptDateNull_doesNotThrow() {
        PaymentReceiptDTO dto = buildDTO();
        dto.setReceiptDate(null);
        Long staffId = 100L;

        assertDoesNotThrow(() -> service.create(dto, staffId));

        ArgumentCaptor<PaymentReceipts> captor = ArgumentCaptor.forClass(PaymentReceipts.class);
        verify(mapper).insertSelective(captor.capture());
        assertNull(captor.getValue().getReceiptDate(), "receiptDate 为 null 时应保持 null");
    }

    // ===== update =====

    @Test
    void update_normalInput_callsUpdateByPrimaryKeySelectiveWithCorrectParams() {
        Long id = 42L;
        PaymentReceiptDTO dto = buildDTO();
        Long staffId = 200L;

        service.update(id, dto, staffId);

        ArgumentCaptor<PaymentReceipts> captor = ArgumentCaptor.forClass(PaymentReceipts.class);
        verify(mapper).updateByPrimaryKeySelective(captor.capture());

        PaymentReceipts captured = captor.getValue();
        assertEquals(id, captured.getId(), "更新时应设置 id");
        assertEquals("SK-20260601-001", captured.getReceiptNo());
        assertEquals(1L, captured.getCustomerId());
        assertEquals(10L, captured.getSalesInvoiceId());
        assertEquals(new BigDecimal("9999.99"), captured.getAmount());
        assertEquals(java.sql.Date.valueOf(LocalDate.of(2026, 6, 1)), captured.getReceiptDate());
        assertEquals("bank", captured.getMethod());
        assertEquals("测试备注", captured.getRemark());
        assertEquals(staffId, captured.getUpdatedById(), "updatedById 应为 staffId");
        // 注意: update 不设置 createdById
        assertNull(captured.getCreatedById(), "update 不应设置 createdById");
    }

    // ===== delete =====

    @Test
    void delete_callsDeleteByPrimaryKey() {
        Long id = 99L;

        service.delete(id);

        verify(mapper).softDelete(id);
    }

    // ===== findPage =====

    @Test
    void findPage_callsSelectPageList() {
        PaymentReceiptQuery query = new PaymentReceiptQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        List<PaymentReceiptVO> mockList = Collections.singletonList(buildVO());
        when(mapper.selectPageList(query)).thenReturn(mockList);

        PageInfo<PaymentReceiptVO> result = service.findPage(query);

        verify(mapper).selectPageList(query);
        assertNotNull(result, "返回的 PageInfo 不应为 null");
        assertEquals(1, result.getList().size(), "列表大小应与 mock 返回一致");
    }

    // ===== helpers =====

    private static PaymentReceiptDTO buildDTO() {
        PaymentReceiptDTO dto = new PaymentReceiptDTO();
        dto.setReceiptNo("SK-20260601-001");
        dto.setCustomerId(1L);
        dto.setSalesInvoiceId(10L);
        dto.setAmount(new BigDecimal("9999.99"));
        dto.setReceiptDate(LocalDate.of(2026, 6, 1));
        dto.setMethod("bank");
        dto.setRemark("测试备注");
        return dto;
    }

    private static PaymentReceiptVO buildVO() {
        PaymentReceiptVO vo = new PaymentReceiptVO();
        vo.setId(1L);
        vo.setReceiptNo("SK-20260601-001");
        vo.setCustomerId(1L);
        vo.setCustomerName("测试客户");
        vo.setAmount(new BigDecimal("9999.99"));
        vo.setMethod("bank");
        return vo;
    }
}
