package com.example.recycling_app.dto.market;

import java.util.List;

public class ApiResponse<T> {
    private String status;
    private String message;
    private String productId;
    private List<T> products;
    private int count;
    private String keyword;

    public ApiResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public List<T> getProducts() { return products; }
    public void setProducts(List<T> products) { this.products = products; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    // 성공 여부 확인 메서드
    public boolean isSuccess() {
        return "success".equals(status);
    }
}