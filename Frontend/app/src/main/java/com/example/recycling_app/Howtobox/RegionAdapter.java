// 파일 경로: com/example/recycling_app/Howtobox/RegionAdapter.java
package com.example.recycling_app.Howtobox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recycling_app.R; // 자신의 R 클래스 경로 확인
import java.util.ArrayList;
import java.util.List;

public class RegionAdapter extends RecyclerView.Adapter<RegionAdapter.RegionViewHolder> {

    private List<String> items = new ArrayList<>();
    private OnItemClickListener listener;

    /**
     * 아이템 클릭 이벤트를 Activity로 전달하기 위한 콜백 인터페이스
     */
    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    /**
     * Activity에서 클릭 리스너를 설정하기 위한 메서드
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * RecyclerView에 표시할 데이터 목록을 업데이트하고 화면을 갱신합니다.
     * @param newItems 새로운 데이터 리스트
     */
    public void setItems(List<String> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged(); // 데이터가 변경되었음을 어댑터에 알려 UI를 새로 그리게 함
    }

    /**
     * 각 아이템의 레이아웃(item_region.xml)을 인플레이트(객체화)하여 ViewHolder를 생성합니다.
     */
    @NonNull
    @Override
    public RegionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.area_item_region, parent, false);
        return new RegionViewHolder(view);
    }

    /**
     * ViewHolder에 실제 데이터를 바인딩(연결)합니다.
     * @param holder 데이터를 표시할 ViewHolder
     * @param position 현재 아이템의 위치(인덱스)
     */
    @Override
    public void onBindViewHolder(@NonNull RegionViewHolder holder, int position) {
        String item = items.get(position);
        holder.bind(item, listener);
    }

    /**
     * 전체 아이템의 개수를 반환합니다.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * 각 아이템 뷰를 보관하는 ViewHolder 클래스
     */
    static class RegionViewHolder extends RecyclerView.ViewHolder {
        TextView regionTextView;

        public RegionViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_region.xml에 정의된 TextView를 찾아 변수에 할당
            regionTextView = itemView.findViewById(R.id.regionTextView);
        }

        /**
         * 실제 데이터를 TextView에 설정하고 클릭 리스너를 연결합니다.
         * @param item 표시할 텍스트 (예: "서울특별시")
         * @param listener 클릭 이벤트를 처리할 리스너
         */
        public void bind(final String item, final OnItemClickListener listener) {
            regionTextView.setText(item);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}