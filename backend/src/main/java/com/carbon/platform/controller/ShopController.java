package com.carbon.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carbon.platform.dto.ApiResponse;
import com.carbon.platform.entity.ProductCategory;
import com.carbon.platform.mapper.ProductCategoryMapper;
import com.carbon.platform.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ProductCategoryMapper productCategoryMapper;

    @GetMapping("/categories")
    public ApiResponse<?> categories() {
        List<ProductCategory> categories = productCategoryMapper.selectList(
            new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder)
        );
        return ApiResponse.success(categories);
    }

    @GetMapping("/products")
    public ApiResponse<?> products(@RequestParam(required = false) String category,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(shopService.getProducts(category, page, size));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<?> productDetail(@PathVariable Long id) {
        return ApiResponse.success(shopService.getProductById(id));
    }

    @PostMapping("/exchange")
    public ApiResponse<?> exchange(@RequestParam Long productId,
                                    @RequestParam(defaultValue = "1") Integer quantity,
                                    @RequestParam(required = false, defaultValue = "") String address,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(shopService.exchangeProduct(userId, productId, quantity, address));
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(shopService.getUserOrders(userId, page, size));
    }
}
