package com.backend.checkout_service.product.controller;

import com.backend.checkout_service.product.dto.ProductResponse;
import com.backend.checkout_service.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Products", description = "Product catalog APIs")
@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam(defaultValue = "0") int offset,
                                                             @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok().body(productService.getAllProducts(offset, limit));
    }
}
