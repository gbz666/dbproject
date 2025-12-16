package com.database.service;

import com.database.mapper.ProductCategoriesMapper;
import com.database.mapper.ProductsMapper;
import com.database.pojo.Products;
import com.database.dto.ProductRequest;
import com.database.vo.ProductVO;
import com.database.exception.BusinessException; // 假设您已经定义
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID; // 用于生成 productCode 示例

@Service
public class ProductsService{

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private ProductCategoriesMapper productCategoriesMapper;

    // ... convertRequestToProducts 方法（用于 DTO -> POJO，逻辑不变）
    private Products convertRequestToProducts(ProductRequest request) {
        // ... 实现 details here (确保 categoryName -> categoryId 逻辑)
        Products product = new Products();
        BeanUtils.copyProperties(request, product);

        Integer categoryId = productCategoriesMapper.selectByName(request.getCategoryName()).getId();
        if (categoryId == null && request.getCategoryName() != null) {
            throw new BusinessException("产品分类名不正确或不存在：" + request.getCategoryName(),400);
        }
        product.setCategoryId(categoryId);
        return product;
    }


    @Transactional
    public ProductVO createProduct(ProductRequest request, Long currentStaffId) {
        // ... 业务逻辑

        Products product = convertRequestToProducts(request);

        // 【重要】生成业务编码
        product.setProductCode("P" + UUID.randomUUID().toString().substring(0, 7).toUpperCase());

        // 审计字段设置
        product.setCreatedById(currentStaffId);
        product.setUpdatedById(currentStaffId);
        product.setIsDeleted(0);

        int rows = productsMapper.insertSelective(product);
        if (rows > 0) {
            return productsMapper.selectVOByProductCode(product.getProductCode());
        }
        throw new BusinessException( "产品创建失败，请重试。",500);
    }

    @Transactional
    public ProductVO updateProduct(String productCode, ProductRequest request, Long currentStaffId) {

        // 1. 根据业务编码查找实际 ID
        Products existingProduct = productsMapper.selectByProductCode(productCode);
        if (existingProduct == null) {
            throw new BusinessException( "产品编码[" + productCode + "]不存在或已被删除。",504);
        }

        // 2. 转换为 POJO 并进行业务翻译
        Products productToUpdate = convertRequestToProducts(request);

        // 3. 核心：设置内部 ID 和审计字段
        productToUpdate.setId(existingProduct.getId()); // 使用查询到的内部ID进行更新
        productToUpdate.setUpdatedById(currentStaffId);

        // 4. 执行更新
        int rows = productsMapper.updateByPrimaryKeySelective(productToUpdate);
        if (rows == 0) {
            // 如果 updateByPrimaryKeySelective 返回 0，可能是传入的字段与原有值相同，MyBatis不会更新
            // 但如果因为产品在更新期间被删除，则应在上一步 selectByProductCode 处捕获。
            // 这里假定是成功但无字段变化，或继续抛出异常
            throw new BusinessException("产品更新失败，可能字段值未变化或数据校验失败。",400);
        }

        // 5. 返回更新后的完整产品信息（VO）
        return productsMapper.selectVOByProductCode(productCode);
    }

    public ProductVO getProductDetail(String productCode) {
        // 直接使用业务编码查询 VO
        ProductVO vo = productsMapper.selectVOByProductCode(productCode);
        if (vo == null) {
            throw new BusinessException("产品编码[" + productCode + "]不存在或已被删除。",404);
        }
        return vo;
    }


    @Transactional
    public boolean deleteProduct(String productCode) {
        // 1. 业务检查：是否有活动库存或订单关联 (略)

        // 2. 执行逻辑删除
        int rows = productsMapper.softDeleteByProductCode(productCode);
        if (rows == 0) {
            throw new BusinessException("产品编码[" + productCode + "]删除失败，产品不存在或已是删除状态。",400);
        }
        return true;
    }

    @Transactional
    public PageInfo<ProductVO> getProductsByPage(
            int pageNum,
            int pageSize,
            String productName,
            String categoryName,
            String productCode) {

        // 1. 启动 PageHelper 分页功能
        // 仅对紧随其后的第一次查询生效
        PageHelper.startPage(pageNum, pageSize);

        // 2. 调用 Mapper 的查询方法（SQL 不变，PageHelper 会自动添加 LIMIT/OFFSET）
        List<ProductVO> productList = productsMapper.selectVOList(productName, categoryName,productCode);

        // 3. 将 List 封装为 PageInfo 返回
        return new PageInfo<>(productList);
    }
}