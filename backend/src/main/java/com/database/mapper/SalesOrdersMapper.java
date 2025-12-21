package com.database.mapper;

import com.database.pojo.SalesOrders;
import com.database.vo.SalesOrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【sales_orders(销售订单表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.SalesOrders
*/
public interface SalesOrdersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesOrders record);



    int countOrdersByMonth(@Param("monthStr") String monthStr);
    int updateByPrimaryKey(SalesOrders record);

    List<SalesOrderVO> selectSalesOrderByPage(@Param("customerName")String customerName,@Param("customerCode")String customerCode,@Param("productCode") String productCode, @Param("productName")String productName,@Param("salesOrderCode") String salesOrderCode);
     // 增（用 insertSelective，没值的字段不插入，用数据库默认值）
    int insertSelective(SalesOrders record);
    // 改
    int updateByPrimaryKeySelective(SalesOrders record);
    // 查单条
    SalesOrders selectByPrimaryKey(Long id);

    Long selectOrderIdByCode(String salesOrderCode);
}
