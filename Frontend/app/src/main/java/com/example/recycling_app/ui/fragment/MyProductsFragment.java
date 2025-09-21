package com.example.recycling_app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recycling_app.R;
import com.example.recycling_app.Upcycling_market.Product_Detail;
import com.example.recycling_app.adapter.ProductAdapter;
import com.example.recycling_app.dto.market.ProductDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MyProductsFragment";

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private TextView tvEmptyMessage;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<ProductDTO> allProducts; // 검색을 위한 전체 제품 목록

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_products, container, false);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefreshLayout();
        loadMyProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMyProducts);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutProducts);
        allProducts = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(productAdapter);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "onRefresh called");
            loadMyProducts();
        });
    }

    private void loadMyProducts() {
        if (currentUserId == null) {
            showEmptyMessage("로그인이 필요합니다.");
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        swipeRefreshLayout.setRefreshing(true); // 로딩 시작
        firestore.collection("products")
                .whereEqualTo("uid", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ProductDTO> myProducts = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            ProductDTO product = document.toObject(ProductDTO.class);
                            product.setProductId(document.getId());
                            myProducts.add(product);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document to ProductDTO", e);
                        }
                    }

                    myProducts.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));

                    allProducts.clear();
                    allProducts.addAll(myProducts); // 전체 목록 저장

                    if (myProducts.isEmpty()) {
                        showEmptyMessage("등록한 제품이 없습니다.");
                    } else {
                        showProducts(myProducts);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading my products", e);
                    Toast.makeText(getContext(), "제품 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    showEmptyMessage("제품을 불러올 수 없습니다.");
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void showProducts(List<ProductDTO> products) {
        tvEmptyMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        productAdapter.setProductList(products);
    }

    private void showEmptyMessage(String message) {
        recyclerView.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    @Override
    public void onProductClick(ProductDTO product) {
        Intent intent = new Intent(getContext(), Product_Detail.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    // 검색 메소드
    public void search(String query) {
        if (allProducts == null) return;

        if (query.isEmpty()) {
            if (allProducts.isEmpty()) {
                showEmptyMessage("등록한 제품이 없습니다.");
            } else {
                showProducts(allProducts);
            }
        } else {
            List<ProductDTO> filteredList = allProducts.stream()
                    .filter(product -> product.getProductName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            if (filteredList.isEmpty()) {
                showEmptyMessage("검색 결과가 없습니다.");
            } else {
                showProducts(filteredList);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyProducts();
    }
}