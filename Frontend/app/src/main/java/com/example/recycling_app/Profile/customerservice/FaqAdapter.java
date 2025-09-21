package com.example.recycling_app.Profile.customerservice;

import android.view.LayoutInflater; // XML 레이아웃을 View 객체로 변환
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.view.ViewGroup; // View 그룹 (레이아웃)
import android.widget.ImageView; // 이미지 뷰
import android.widget.LinearLayout; // LinearLayout 위젯 (레이아웃 그룹)
import android.widget.TextView; // 텍스트 뷰

import androidx.recyclerview.widget.RecyclerView; // 스크롤 가능한 대량의 항목을 효율적으로 표시

import com.example.recycling_app.R;
import com.example.recycling_app.dto.FaqDTO; // FAQ 데이터 DTO (FaqDTO가 이 패키지에 있다고 가정)

import java.util.List; // 리스트 컬렉션

// FAQ 목록을 RecyclerView에 표시하기 위한 어댑터 클래스
// FaqDTO 객체 리스트를 받아 RecyclerView의 각 항목(질문-답변)으로 변환하고 표시
public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private final List<FaqDTO> localFaqList; // 어댑터가 사용할 FAQ 데이터 리스트

    // 어댑터 생성자: FAQ 데이터 리스트를 받아 초기화
    public FaqAdapter(List<FaqDTO> faqList) {
        this.localFaqList = faqList;
    }

    @Override
    // ViewHolder 생성: RecyclerView의 각 항목에 해당하는 View를 생성하고 ViewHolder에 담아 반환
    public FaqViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 중요: `item_faq.xml` 레이아웃 파일을 인플레이트하여 View 객체 생성
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    // ViewHolder에 데이터 바인딩: 특정 위치(position)의 FaqDTO 데이터를 ViewHolder의 View에 설정
    public void onBindViewHolder(FaqViewHolder holder, int position) {
        FaqDTO faq = localFaqList.get(position); // 현재 위치의 FAQ 객체 가져오기
        holder.tvQuestion.setText(faq.getQuestion()); // 질문 텍스트 설정
        holder.tvAnswer.setText(faq.getAnswer()); // 답변 텍스트 설정


        if (holder.answerContainer.getVisibility() == View.VISIBLE) {
            // 답변이 보이면 (펼쳐진 상태) 위쪽 화살표 표시
            holder.ivExpandCollapse.setImageResource(R.drawable.outline_arrow_drop_up_24); // 위쪽 화살표
        } else {
            // 답변이 숨겨져 있으면 (접힌 상태) 아래쪽 화살표 표시
            holder.ivExpandCollapse.setImageResource(R.drawable.outline_arrow_drop_down_24); // 아래쪽 화살표
        }

        // 질문 헤더(`questionHeader`) 클릭 리스너 설정
        // 클릭 시 답변 컨테이너의 가시성을 토글하고, 이에 따라 화살표 이미지 변경
        holder.questionHeader.setOnClickListener(v -> {
            if (holder.answerContainer.getVisibility() == View.GONE) {
                // 현재 답변이 숨겨져 있으면 보이도록 변경하고 화살표를 위쪽으로 변경
                holder.answerContainer.setVisibility(View.VISIBLE);
                holder.ivExpandCollapse.setImageResource(R.drawable.outline_arrow_drop_up_24);
            } else {
                // 현재 답변이 보이면 숨기도록 변경하고 화살표를 아래쪽으로 변경
                holder.answerContainer.setVisibility(View.GONE);
                holder.ivExpandCollapse.setImageResource(R.drawable.outline_arrow_drop_down_24);
            }
        });
    }

    @Override
    // 전체 항목 개수 반환
    public int getItemCount() {
        return localFaqList.size();
    }

    // --- ViewHolder 클래스 ---
    // RecyclerView의 각 FAQ 항목에 대한 뷰들을 보유
    public static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion; // 질문 텍스트 뷰
        TextView tvAnswer; // 답변 텍스트 뷰
        LinearLayout questionHeader; // 질문 영역을 감싸는 레이아웃 (클릭 이벤트 처리용)
        LinearLayout answerContainer; // 답변 내용을 감싸는 레이아웃 (가시성 토글용)
        ImageView ivExpandCollapse; // 확장/축소 화살표 이미지 뷰

        // ViewHolder 생성자: `item_faq.xml`에 정의된 뷰 ID를 사용하여 뷰를 찾고 초기화
        public FaqViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_faq_question);
            tvAnswer = itemView.findViewById(R.id.tv_faq_answer);
            questionHeader = itemView.findViewById(R.id.question_header);
            answerContainer = itemView.findViewById(R.id.faq_answer_container);
            ivExpandCollapse = itemView.findViewById(R.id.iv_expand_collapse);
        }
    }
}