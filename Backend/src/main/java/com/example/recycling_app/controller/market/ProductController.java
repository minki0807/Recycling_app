package com.example.recycling_app.controller.market;

import com.example.recycling_app.dto.market.ProductDto;
import com.example.recycling_app.service.market.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerProduct(
            @RequestPart("product") ProductDto product,
            @RequestPart("images") List<MultipartFile> imageFiles
    ) {
        Map<String, Object> response = new HashMap<>();

        if (imageFiles.size() > 10) { // 사진 업로드 최대 개수 제한
            response.put("status", "error");
            response.put("message", "이미지는 최대 10장까지 업로드할 수 있습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            String productId = productService.registerProduct(product, imageFiles);
            response.put("status", "success");
            response.put("productId", productId);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 모든 상품 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ProductDto> products = productService.getAllProducts();
            response.put("status", "success");
            response.put("products", products);
            response.put("count", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품명 검색
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ProductDto> products = productService.searchProductsByName(keyword);
            response.put("status", "success");
            response.put("products", products);
            response.put("count", products.size());
            response.put("keyword", keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String productId,
            @RequestPart("product") ProductDto productDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        Map<String, Object> response = new HashMap<>();

        if (imageFiles != null && imageFiles.size() > 10) {
            response.put("status", "error");
            response.put("message", "이미지는 최대 10장까지 업로드할 수 있습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            ProductDto updatedProduct = productService.updateProduct(productId, productDto, imageFiles);
            response.put("status", "success");
            response.put("message", "상품이 성공적으로 수정되었습니다.");
            response.put("product", updatedProduct);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "서버 내부 오류가 발생했습니다.");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<Object, Object>> deleteProduct(
            @PathVariable String productId,
            @RequestParam String uid) {
        Map<Object, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(productId, uid);
            response.put("status", "success");
            response.put("message", "상품이 성공적으로 삭제되었습니다.");
            response.put("productId", productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}