package com.example.recycling_app.Profile.profieedit;

import android.content.Context; // Android 애플리케이션 환경 정보에 접근
import android.net.Uri; // 리소스 식별자
import android.view.LayoutInflater; // XML 레이아웃을 View 객체로 변환
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.view.ViewGroup; // View 그룹 (레이아웃)
import android.widget.ImageView; // 이미지 뷰

import androidx.annotation.NonNull; // null이 아님을 명시하는 어노테이션
import androidx.recyclerview.widget.RecyclerView; // 스크롤 가능한 대량의 항목을 효율적으로 표시

import com.bumptech.glide.Glide; // 이미지 로딩 라이브러리 Glide
import com.example.recycling_app.R;

import java.util.List; // 리스트 컬렉션

// 갤러리 이미지를 RecyclerView에 표시하기 위한 어댑터 클래스
// 이미지 URI 리스트를 받아 RecyclerView의 각 항목(이미지 뷰)으로 변환하고 표시
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

    private final Context context; // 어댑터가 실행되는 애플리케이션 컨텍스트
    private final List<Uri> imageUris; // 표시할 이미지들의 URI 리스트
    private final OnImageClickListener listener; // 이미지 클릭 이벤트를 처리할 리스너

    // 이미지 클릭 시 호출될 콜백 인터페이스
    public interface OnImageClickListener {
        void onImageClick(Uri imageUri); // 클릭된 이미지의 URI를 전달
    }

    // 어댑터 생성자
    // 컨텍스트, 이미지 URI 리스트, 이미지 클릭 리스너를 받아 초기화
    public GalleryAdapter(Context context, List<Uri> imageUris, OnImageClickListener listener) {
        this.context = context;
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    // ViewHolder 생성: RecyclerView의 각 항목에 해당하는 View를 생성하고 ImageViewHolder에 담아 반환
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // `image_item_layout.xml` 레이아웃 파일을 인플레이트하여 View 객체 생성
        View view = LayoutInflater.from(context).inflate(R.layout.image_item_layout, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    // ViewHolder에 데이터 바인딩: 특정 위치(position)의 이미지 URI를 ViewHolder의 ImageView에 설정
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position); // 현재 위치의 이미지 URI 가져오기
        // Glide를 사용하여 이미지 로드 (효율적인 이미지 로딩 및 캐싱)
        Glide.with(context) // 컨텍스트 제공
                .load(imageUri) // 로드할 이미지 URI
                .centerCrop() // 이미지를 중앙에 맞추어 자르기
                .placeholder(R.drawable.basic_profile_logo) // 이미지를 로딩 중일 때 표시될 이미지
                .error(R.drawable.basic_profile_logo) // 이미지 로드 실패 시 표시될 이미지
                .into(holder.imageView); // 이미지를 표시할 ImageView

        // 항목 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> {
            // 리스너가 null이 아니면, 클릭된 이미지의 URI를 리스너에게 전달
            if (listener != null) {
                listener.onImageClick(imageUri);
            }
        });
    }

    @Override
    // 전체 항목 개수 반환
    public int getItemCount() {
        return imageUris.size(); // 이미지 URI 리스트의 크기 반환
    }

    // --- ViewHolder 클래스 ---
    // RecyclerView의 각 이미지 항목에 대한 뷰를 보유
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // 이미지를 표시할 ImageView

        // ViewHolder 생성자: `image_item_layout.xml`에 정의된 뷰 ID를 사용하여 ImageView를 찾고 초기화
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_image_view);
        }
    }
}