package com.database.controller;

import com.database.dto.PaymentExpenseDTO;
import com.database.dto.PaymentExpenseQuery;
import com.database.service.PaymentExpenseService;
import com.database.vo.PaymentExpenseVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * PaymentExpenseController 单元测试。
 * 纯 POJO 测试，mock service 验证控制器返回值和 HTTP 状态码。
 */
class PaymentExpenseControllerTest {

    private PaymentExpenseService service;
    private PaymentExpenseController controller;

    @BeforeEach
    void setUp() {
        service = mock(PaymentExpenseService.class);
        controller = new PaymentExpenseController();
        ReflectionTestUtils.setField(controller, "service", service);
    }

    // ===== getPage =====

    @Test
    void getPage_returnsOkWithPageInfo() {
        PaymentExpenseQuery query = new PaymentExpenseQuery();
        query.setPageNum(1);
        query.setPageSize(10);

        PaymentExpenseVO vo = new PaymentExpenseVO();
        vo.setId(1L);
        vo.setPaymentNo("PAY-001");
        vo.setAmount(new BigDecimal("500.00"));
        PageInfo<PaymentExpenseVO> pageInfo = new PageInfo<>(List.of(vo));
        when(service.findPage(query)).thenReturn(pageInfo);

        ResponseEntity<Result<PageInfo<PaymentExpenseVO>>> response = controller.getPage(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getList().size());
        assertEquals("PAY-001", response.getBody().getData().getList().get(0).getPaymentNo());
        verify(service).findPage(query);
    }

    // ===== create =====

    @Test
    void create_returnsCreatedStatus() {
        PaymentExpenseDTO dto = new PaymentExpenseDTO();
        dto.setPaymentNo("PAY-002");
        Long staffId = 10L;

        ResponseEntity<Result<Void>> response = controller.create(dto, staffId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        verify(service).create(dto, staffId);
    }

    // ===== update =====

    @Test
    void update_returnsOkWithMessage() {
        PaymentExpenseDTO dto = new PaymentExpenseDTO();
        dto.setPaymentNo("PAY-003");
        Long id = 5L;
        Long staffId = 10L;

        ResponseEntity<Result<Void>> response = controller.update(id, dto, staffId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("更新成功", response.getBody().getMessage());
        verify(service).update(id, dto, staffId);
    }

    // ===== delete =====

    @Test
    void delete_returnsOkWithMessage() {
        Long id = 7L;

        ResponseEntity<Result<Void>> response = controller.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("删除成功", response.getBody().getMessage());
        verify(service).delete(id);
    }
}
