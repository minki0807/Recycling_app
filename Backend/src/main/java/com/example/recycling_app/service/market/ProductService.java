package com.example.recycling_app.service.market;

import com.example.recycling_app.dto.market.ProductDto;
import com.example.recycling_app.service.FirebaseStorageService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.storage.Blob;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class ProductService {

    public static final String COLLECTION_NAME = "products"; // Firestore 컬렉션 이름
    public static final String PRODUCTS_FOLDER = "products_images"; // Storage 폴더명

    private final FirebaseStorageService firebaseStorageService;

    public ProductService(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    // 상품 등록
    public String registerProduct(ProductDto product, List<MultipartFile> imageFiles) throws Exception {
        if (product == null) {
            throw new IllegalArgumentException("상품 정보가 필요합니다.");
        }

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("최소 1장의 이미지가 필요합니다.");
        }

        Firestore db = FirestoreClient.getFirestore();

        try {
            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = firebaseStorageService.uploadFile(file, PRODUCTS_FOLDER);
                    imageUrls.add(imageUrl);
                }
            }
            if (imageUrls.isEmpty()) {
                throw new RuntimeException("유효한 이미지가 없어 업로드에 실패했습니다.");
            }

            product.setImages(imageUrls);
            String productId = UUID.randomUUID().toString();
            product.setProductId(productId);
            product.setCreatedAt(System.currentTimeMillis());

            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).set(product);
            System.out.println("상품 등록 완료 - Update time: " + future.get().getUpdateTime());
            System.out.println("상품 ID: " + productId + ", 이미지 개수: " + imageUrls.size());

            return productId;

        } catch (Exception e) {
            System.err.println("상품 등록 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 모든 상품 조회
    public List<ProductDto> getAllProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ProductDto> products = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            try {
                ProductDto product = document.toObject(ProductDto.class);
                products.add(product);
            } catch (Exception e) {
                System.err.println("상품 데이터 변환 실패 - Document ID: " + document.getId());
            }
        }

        System.out.println("전체 상품 조회 완료 - 총 " + products.size() + "개");
        return products;
    }

    // 상품명으로 검색
    public List<ProductDto> searchProductsByName(String keyword) throws ExecutionException, InterruptedException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(); // 키워드가 없으면 전체 조회
        }

        Firestore db = FirestoreClient.getFirestore();
        String lowercaseKeyword = keyword.toLowerCase().trim();

        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ProductDto> products = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            try {
                ProductDto product = document.toObject(ProductDto.class);

                boolean nameMatch = product.getProductName() != null &&
                        product.getProductName().toLowerCase().contains(lowercaseKeyword);

                boolean descMatch = product.getProductDescription() != null &&
                        product.getProductDescription().toLowerCase().contains(lowercaseKeyword);

                if (nameMatch || descMatch) {
                    products.add(product);
                }
            } catch (Exception e) {
                System.err.println("상품 검색 중 데이터 변환 실패 - Document ID: " + document.getId());
            }
        }

        System.out.println("상품 검색 완료 - 키워드: '" + keyword + "', 결과: " + products.size() + "개");
        return products;
    }

    // 상품 수정
    public ProductDto updateProduct(String productId, ProductDto updatedData, List<MultipartFile> imageFiles) throws Exception {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }
        if (updatedData == null || updatedData.getUid() == null) {
            throw new IllegalArgumentException("업데이트 정보와 사용자 ID가 필요합니다.");
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);

        try {
            // 1. 기존 상품 정보 조회
            DocumentSnapshot document = productRef.get().get();
            if (!document.exists()) {
                throw new RuntimeException("수정할 상품을 찾을 수 없습니다.");
            }
            ProductDto existingProduct = document.toObject(ProductDto.class);
            if (existingProduct == null) {
                throw new RuntimeException("상품 데이터를 읽을 수 없습니다.");
            }

            // 2. 소유권 확인
            if (!existingProduct.getUid().equals(updatedData.getUid())) {
                throw new SecurityException("자신의 상품만 수정할 수 있습니다.");
            }

            // 3. 업데이트할 필드 맵 생성
            Map<String, Object> updates = new HashMap<>();
            updates.put("productName", updatedData.getProductName());
            updates.put("productDescription", updatedData.getProductDescription());
            updates.put("price", updatedData.getPrice());
            updates.put("transactionType", updatedData.getTransactionType());

            // 4. 이미지 처리 (새 이미지가 제공된 경우)
            if (imageFiles != null && !imageFiles.isEmpty() && imageFiles.stream().anyMatch(f -> !f.isEmpty())) {
                // 기존 이미지 삭제
                List<String> oldImageUrls = existingProduct.getImages();
                if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
                    for (String url : oldImageUrls) {
                        try {
                            firebaseStorageService.deleteFile(url);
                        } catch (Exception e) {
                            System.err.println("Storage 기존 파일 삭제 실패: " + url + ", 에러: " + e.getMessage());
                        }
                    }
                }

                // 새 이미지 업로드
                List<String> newImageUrls = new ArrayList<>();
                for (MultipartFile file : imageFiles) {
                    if (file != null && !file.isEmpty()) {
                        String imageUrl = firebaseStorageService.uploadFile(file, PRODUCTS_FOLDER);
                        newImageUrls.add(imageUrl);
                    }
                }
                if (newImageUrls.isEmpty()) {
                    throw new RuntimeException("유효한 이미지가 없어 업로드에 실패했습니다.");
                }
                updates.put("images", newImageUrls);
                updatedData.setImages(newImageUrls); // DTO에도 반영
            } else {
                // 새 이미지가 없으면 기존 이미지 URL을 유지
                updatedData.setImages(existingProduct.getImages());
            }

            // 5. Firestore 문서 업데이트
            productRef.update(updates).get();
            System.out.println("상품 수정 완료: " + productId);

            // 6. 업데이트된 전체 DTO 반환
            updatedData.setProductId(existingProduct.getProductId());
            updatedData.setCreatedAt(existingProduct.getCreatedAt());

            return updatedData;

        } catch (Exception e) {
            System.err.println("상품 수정 실패: " + e.getMessage());
            throw new RuntimeException("상품 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 상품 삭제
    public void deleteProduct(String productId, String uid) throws Exception {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 ID가 필요합니다.");
        }

        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        Firestore db = FirestoreClient.getFirestore();
        System.out.println("삭제 요청 - ProductId: " + productId + ", UID: " + uid);

        try {
            // 상품 정보 조회
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(productId).get().get();
            if (!document.exists()) {
                System.out.println("상품이 존재하지 않음: " + productId);
                throw new RuntimeException("상품을 찾을 수 없습니다.");
            }

            ProductDto product = document.toObject(ProductDto.class);
            if (product == null) {
                throw new RuntimeException("상품 데이터를 읽을 수 없습니다.");
            }

            System.out.println("상품 소유자 UID: " + product.getUid());
            System.out.println("요청자 UID: " + uid);

            // 상품 소유자 확인
            if (!uid.equals(product.getUid())) {
                System.out.println("권한 없음 - 소유자: " + product.getUid() + ", 요청자: " + uid);
                throw new RuntimeException("자신의 상품만 삭제할 수 있습니다.");
            }

            // Firebase Storage의 이미지 파일들 삭제
            List<String> imageUrls = product.getImages();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String url : imageUrls) {
                    try {
                        firebaseStorageService.deleteFile(url);
                    } catch (Exception e) {
                        System.err.println("Storage 파일 삭제 실패: " + url + ", 에러: " + e.getMessage());
                    }
                }
            }

            // 상품 삭제
            ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).delete();
            System.out.println("Delete time: " + future.get().getUpdateTime());
            System.out.println("상품 삭제 완료: " + productId);

        } catch (Exception e) {
            System.err.println("상품 삭제 실패: " + e.getMessage());
            throw new RuntimeException("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}