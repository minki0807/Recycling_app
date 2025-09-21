package com.example.recycling_app.service;

import com.example.recycling_app.dto.market.ProductDTO;
import com.example.recycling_app.dto.market.ApiResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ProductApiService {

    @Multipart
    @POST("/api/products")
    Call<ApiResponse<Object>> registerProduct(
            @Part("product") RequestBody product,
            @Part List<MultipartBody.Part> images
    );

    @GET("/api/products")
    Call<ApiResponse<ProductDTO>> getAllProducts();

    @GET("/api/products/search")
    Call<ApiResponse<ProductDTO>> searchProducts(@Query("keyword") String keyword);

    @DELETE("/api/products/{productId}")
    Call<ApiResponse<Object>> deleteProduct(
            @Path("productId") String productId,
            @Query("uid") String uid
    );
}