package com.example.recycling_app.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.adapter.SelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectionDialogFragment extends DialogFragment {

    // 선택된 값을 전달하기 위한 인터페이스 정의
    public interface OnItemSelectedListener {
        void onItemSelected(String key, String selectedItem);
    }

    private OnItemSelectedListener listener;
    private String dialogTitle;
    private String selectionKey; // 어떤 종류의 선택인지 구분하기 위한 키 (예: "age", "gender", "region")
    private List<String> dataList;

    // 다이얼로그 생성 시 필요한 데이터와 리스너를 전달받는 팩토리 메서드
    public static SelectionDialogFragment newInstance(String title, String key, ArrayList<String> dataList) {
        SelectionDialogFragment fragment = new SelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("key", key);
        args.putStringArrayList("dataList", dataList); // ArrayList로 전달
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dialogTitle = getArguments().getString("title");
            selectionKey = getArguments().getString("key");
            dataList = getArguments().getStringArrayList("dataList");
        }
    }

    // 리스너를 설정하는 메서드 (Activity에서 호출)
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_selection_list, null); // 새로 생성할 다이얼로그 레이아웃

        TextView titleTextView = view.findViewById(R.id.dialog_title);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_dialog_selection);

        titleTextView.setText(dialogTitle);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SelectionAdapter adapter = new SelectionAdapter(dataList, item -> {
            if (listener != null) {
                listener.onItemSelected(selectionKey, item); // 선택된 값과 키를 리스너로 전달
            }
            dismiss(); // 다이얼로그 닫기
        });
        recyclerView.setAdapter(adapter);

        builder.setView(view);
        return builder.create();
    }
}