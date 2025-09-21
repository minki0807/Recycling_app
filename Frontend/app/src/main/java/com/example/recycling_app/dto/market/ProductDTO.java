package com.example.recycling_app.dto.market;

import java.io.Serializable;
import java.util.List;

public class ProductDTO implements Serializable {
    private String productId;
    private String uid;
    private String productName;
    private String productDescription;
    private int price;
    private List<String> images;
    private long createdAt;
    private String transactionType;

    public ProductDTO() {}

    public ProductDTO(String productId, String uid, String productName, String productDescription, int price, List<String> images,
                      long createdAt, String transactionType) {
        this.productId = productId;
        this.uid = uid;
        this.productName = productName;
        this.productDescription = productDescription;
        this.price = price;
        this.images = images;
        this.createdAt = createdAt;
        this.transactionType = transactionType;
    }


    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}