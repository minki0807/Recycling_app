package com.example.recycling_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.market.ProductDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDTO> productList = new ArrayList<>();
    private OnProductClickListener onProductClickListener;
    private Context context;

    public interface OnProductClickListener {
        void onProductClick(ProductDTO product);
    }

    public ProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDTO product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void setProductList(List<ProductDTO> productList) {
        this.productList = productList != null ? productList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewTransactionType;
        private TextView textViewCreatedAt;
        private TextView textViewDescription;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.iv_product_image);
            textViewName = itemView.findViewById(R.id.tv_product_name);
            textViewPrice = itemView.findViewById(R.id.tv_product_price);
            textViewTransactionType = itemView.findViewById(R.id.tv_transaction_type);
            textViewCreatedAt = itemView.findViewById(R.id.tv_created_at);
            textViewDescription = itemView.findViewById(R.id.tv_product_description);

            // 아이템 클릭 리스너
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onProductClickListener != null) {
                    onProductClickListener.onProductClick(productList.get(position));
                }
            });
        }

        public void bind(ProductDTO product) {
            if (product == null) return;

            // 상품명
            textViewName.setText(product.getProductName() != null ? product.getProductName() : "");

            // 가격 포맷팅
            String formattedPrice = "";
            if ("나눔하기".equals(product.getTransactionType())) {
                formattedPrice = "나눔";
                textViewPrice.setTextColor(context.getResources().getColor(R.color.success, context.getTheme()));
            } else {
                NumberFormat formatter = NumberFormat.getInstance(Locale.KOREA);
                formattedPrice = formatter.format(product.getPrice()) + "원";
                textViewPrice.setTextColor(context.getResources().getColor(R.color.primary_color, context.getTheme()));
            }
            textViewPrice.setText(formattedPrice);

            // 거래 방식
            textViewTransactionType.setText(product.getTransactionType() != null ? product.getTransactionType() : "");

            // 거래 방식에 따른 배경색 설정
            if ("나눔하기".equals(product.getTransactionType())) {
                textViewTransactionType.setBackgroundResource(R.drawable.transaction_share_background);
            } else {
                textViewTransactionType.setBackgroundResource(R.drawable.transaction_sell_background);
            }

            // 등록 시간
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.KOREA);
            String formattedDate = dateFormat.format(new Date(product.getCreatedAt()));
            textViewCreatedAt.setText(formattedDate);

            // 상품 설명 (최대 2줄)
            if (product.getProductDescription() != null && !product.getProductDescription().isEmpty()) {
                textViewDescription.setText(product.getProductDescription());
                textViewDescription.setVisibility(View.VISIBLE);
            } else {
                textViewDescription.setVisibility(View.GONE);
            }

            // 첫 번째 이미지 로드
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String imageUrl = product.getImages().get(0);

                Log.d("ProductAdapter", "Loading image URL: " + imageUrl);

                RequestOptions requestOptions = new RequestOptions()
                        .fitCenter()
                        .transform(new RoundedCorners(24))
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image);

                Glide.with(context)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .into(imageViewProduct);
            } else {
                Log.d("ProductAdapter", "Image list is empty for product: " + product.getProductName());
                // 기본 이미지 설정
                imageViewProduct.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
