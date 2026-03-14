package com.backend.checkout_service.product.service;

import com.backend.checkout_service.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(int offset, int limit);
}
