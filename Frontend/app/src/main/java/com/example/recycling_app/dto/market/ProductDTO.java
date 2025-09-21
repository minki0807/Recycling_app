package com.example.recycling_app.dto.market;

import java.io.Serializable;
import java.util.List;

public class ProductDTO implements Serializable {
    private String productId;
    private String productName;
    private String productDescription;
    private double price;
    private String transactionType;
    private List<String> images;
    private long createdAt;
    private String uid; // 제품을 등록한 사용자의 UID
    private String location;
    private String category;
    private boolean isAvailable;

    // 기본 생성자 (Firestore 직렬화를 위해 필요)
    public ProductDTO() {}

    // 생성자
    public ProductDTO(String productName, String productDescription, double price,
                      String transactionType, List<String> images, String uid) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.price = price;
        this.transactionType = transactionType;
        this.images = images;
        this.uid = uid;
        this.createdAt = System.currentTimeMillis();
        this.isAvailable = true;
    }

    // Getter and Setter methods
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", price=" + price +
                ", transactionType='" + transactionType + '\'' +
                ", uid='" + uid + '\'' +
                ", createdAt=" + createdAt +
                ", isAvailable=" + isAvailable +
                '}';
    }
}